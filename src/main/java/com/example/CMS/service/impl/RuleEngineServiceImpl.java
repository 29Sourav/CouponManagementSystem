package com.example.CMS.service.impl;

import com.example.CMS.entity.Coupon;
import com.example.CMS.entity.CouponRuleTemplate;
import com.example.CMS.model.cart.Cart;

import com.example.CMS.model.rule.DiscountDecision;
import com.example.CMS.repository.CouponRepository;
import com.example.CMS.repository.CouponRuleTemplateRepository;
import com.example.CMS.service.RuleEngineService;
import com.example.CMS.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleEngineServiceImpl implements RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineServiceImpl.class);

    private final CouponRepository couponRepository;
    private final CouponRuleTemplateRepository templateRepository;
    private final TemplateService templateService;

    /**
     * Cache: couponId -> compiled KieContainer
     */
    private final Map<Long, KieContainer> kieContainerCache = new ConcurrentHashMap<>();

    /**
     * Evaluate all coupons and return a list of applicable DiscountDecision objects.
     * Cart is not mutated.
     */
    @Override
    public List<DiscountDecision> evaluateAllCoupons(Cart cart) {
        List<Coupon> coupons = couponRepository.findAll();
        List<DiscountDecision> decisions = new ArrayList<>();

        for (Coupon coupon : coupons) {

            if (Boolean.FALSE.equals(coupon.getActive())) {
                continue;
            }
            if (!isWithinDateRange(coupon)) {
                continue;
            }

            try {
                DiscountDecision dd = runRuleForCoupon(coupon, cart);
                if (dd != null && dd.isApplicable()) {
                    decisions.add(dd);
                }
            } catch (Exception ex) {
                log.error("Error evaluating coupon {}: {}", coupon.getId(), ex.getMessage(), ex);
            }
        }

        decisions.sort(Comparator.comparingDouble(DiscountDecision::getDiscountAmount).reversed());
        return decisions;
    }

    /**
     * Apply a single coupon to the provided cart. Runs the coupon rule to obtain the DiscountDecision
     * and then applies the discount in Java (proportional distribution across items).
     */
    @Override
    public Cart applyCouponToCart(Long couponId, Cart cart) {

        double total = cart.getTotalAmount();
        cart.setTotalAmount(total);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (Boolean.FALSE.equals(coupon.getActive())) {
            log.info("Coupon {} is inactive", couponId);
            return cart;
        }

        if (!isWithinDateRange(coupon)) {
            log.info("Coupon {} is out of date range", couponId);
            return cart;
        }

        DiscountDecision decision = runRuleForCoupon(coupon, cart);
        if (decision != null && decision.isApplicable() && decision.getDiscountAmount() > 0) {
            applyDiscountToCart(cart, decision);
        } else {
            log.info("No applicable discount found for coupon {}", couponId);
            cart.setTotalDiscount(0.0);
        }

        return finalizeCart(cart);
    }

    /**
     * Invalidate compiled artifact for a coupon id.
     */
    @Override
    public void invalidateCacheForCoupon(Long couponId) {
        kieContainerCache.remove(couponId);
        log.info("Cache invalidated for coupon {}", couponId);
    }

    // -------------------------
    // Internal helpers
    // -------------------------


    private Cart finalizeCart(Cart cart) {
        double total = cart.getTotalAmount();
        double discount = cart.getTotalDiscount();
        cart.setFinalAmount(total - discount);
        return cart;
    }


    private DiscountDecision runRuleForCoupon(Coupon coupon, Cart cart) {
        CouponRuleTemplate template = resolveTemplate(coupon);
        if (template == null) {
            log.warn("Rule template missing for coupon {}", coupon.getId());
            return null;
        }

        String drl = templateService.generateDRL(coupon, template);
        log.info("Generated DRL for coupon {}:\n{}", coupon.getId(), drl);

        if (drl == null || drl.isBlank()) {
            log.warn("Generated DRL is empty for coupon {}", coupon.getId());
            return null;
        }

        KieContainer container = getOrBuildKieContainer(coupon.getId(), drl);
        KieSession ksession = container.newKieSession();

        try {
            ksession.insert(cart);
            ksession.insert(coupon);

            ksession.fireAllRules();

            Collection<?> facts = ksession.getObjects(new ClassObjectFilter(DiscountDecision.class));

            return facts.stream()
                    .map(obj -> (DiscountDecision) obj)
                    .filter(dd -> {
                        if (dd.getCouponId() == null) {
                            dd.setCouponId(coupon.getId());
                        }
                        return Objects.equals(dd.getCouponId(), coupon.getId());
                    })
                    .findFirst()
                    .orElse(null);

        } finally {
            ksession.dispose();
        }

    }

    private KieContainer getOrBuildKieContainer(Long couponId, String drl) {
        return kieContainerCache.computeIfAbsent(couponId, id -> {
            log.info("Compiling DRL for coupon {} (cache miss)", id);
            return buildKieContainer(drl);
        });
    }

    private KieContainer buildKieContainer(String drl) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        String drlPath = "src/main/resources/rules/dynamic-" + UUID.randomUUID() + ".drl";
        Resource drlResource = ks.getResources()
                .newByteArrayResource(drl.getBytes())
                .setResourceType(ResourceType.DRL)
                .setSourcePath(drlPath);

        kfs.write(drlResource);

        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();
        Results results = builder.getResults();

        if (results.hasMessages(Message.Level.ERROR)) {
            String errors = results.getMessages().stream()
                    .map(Message::getText)
                    .collect(Collectors.joining("\n"));
            log.error("DRL compilation errors:\n{}", errors);
            throw new RuntimeException("Failed to compile DRL: \n" + errors);
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }

    private boolean isWithinDateRange(Coupon coupon) {
        try {
            if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(java.time.LocalDate.now())) {
                return false;
            }
            if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(java.time.LocalDate.now())) {
                return false;
            }
        } catch (Exception ignore) {
            // allow evaluation if date check fails unexpectedly
        }
        return true;
    }

    private CouponRuleTemplate resolveTemplate(Coupon coupon) {
        CouponRuleTemplate t = coupon.getRuleTemplate();
        if (t != null) {
            return t;
        }

        try {
            // defensive fetch if proxy/nullable
            Long templateId = coupon.getRuleTemplate().getId();
            return templateRepository.findById(templateId).orElse(null);
        } catch (Exception ignore) {
            return null;
        }
    }

    private void applyDiscountToCart(Cart cart, DiscountDecision decision) {
//        double total = cart.getTotalAmount();
//        if (total <= 0 || decision.getDiscountAmount() <= 0) {
//            return;
//        }

//        List<CartItem> items = cart.getItems();
//        if (items == null || items.isEmpty()) {
//            return;
//        }

//        double remaining = decision.getDiscountAmount();
//
//        for (int i = 0; i < items.size(); i++) {
//            CartItem item = items.get(i);
//            double itemTotal = item.getPrice() * item.getQuantity();
//
//            double share;
//            if (i == items.size() - 1) {
//                share = Math.min(remaining, itemTotal);
//            } else {
//                share = Math.min((itemTotal / total) * decision.getDiscountAmount(), itemTotal);
//                remaining -= share;
//            }
//
//            double newPrice = item.getPrice() - (share / Math.max(1, item.getQuantity()));
//            item.setPrice(Math.max(newPrice, 0.0));

        if (cart == null || decision == null) return;

        double discount = decision.getDiscountAmount();
        if (discount <= 0) return;

        cart.setTotalDiscount(discount);

    }
}
