package com.example.CMS.service.impl;

import com.example.CMS.entity.Coupon;
import com.example.CMS.entity.CouponRuleTemplate;
import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.dto.CreateCouponRequest;
import com.example.CMS.model.dto.CouponResponse;
import com.example.CMS.model.rule.DiscountDecision;
import com.example.CMS.repository.CouponRepository;
import com.example.CMS.repository.CouponRuleTemplateRepository;
import com.example.CMS.service.CouponService;
import com.example.CMS.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponRuleTemplateRepository templateRepository;
    private final RuleEngineService ruleEngineService;

    @Override
    public CouponResponse createCoupon(CreateCouponRequest request) {

        CouponRuleTemplate template = templateRepository
                .findById(request.getRuleTemplateId())
                .orElseThrow(() -> new RuntimeException("Rule template not found"));

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .type(request.getType())
                .active(request.getActive())
                .startDate(parseDate(request.getStartDate()))
                .endDate(parseDate(request.getEndDate()))
                .metadata(request.getMetadata())
                .ruleTemplate(template)
                .build();

        couponRepository.save(coupon);
        return toResponse(coupon);
    }


    @Override
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        return toResponse(coupon);
    }

    @Override
    public CouponResponse updateCoupon(Long id, CreateCouponRequest request) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        CouponRuleTemplate template = templateRepository
                .findById(request.getRuleTemplateId())
                .orElseThrow(() -> new RuntimeException("Rule template not found"));

        coupon.setCode(request.getCode());
        coupon.setType(request.getType());
        coupon.setActive(request.getActive());
        coupon.setStartDate(LocalDate.parse(request.getStartDate()));
        coupon.setEndDate(LocalDate.parse(request.getEndDate()));
        coupon.setMetadata(request.getMetadata());
        coupon.setRuleTemplate(template);

        couponRepository.save(coupon);
        return toResponse(coupon);
    }

    @Override
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    public List<DiscountDecision> getApplicableCoupons(Cart cart) {
        return ruleEngineService.evaluateAllCoupons(cart);
    }

    @Override
    public Cart applyCoupon(Long id, Cart cart) {
        return ruleEngineService.applyCouponToCart(id, cart);
    }
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null; // allow nullable dates
        }
        return LocalDate.parse(dateString); // expects ISO yyyy-MM-dd
    }


    private CouponResponse toResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType())
                .active(coupon.getActive())
                .startDate(coupon.getStartDate().toString())
                .endDate(coupon.getEndDate().toString())
                .metadata(coupon.getMetadata())
                .ruleTemplateId(coupon.getRuleTemplate().getId())
                .build();
    }
}
