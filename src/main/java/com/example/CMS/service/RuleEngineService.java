package com.example.CMS.service;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.rule.DiscountDecision;

import java.util.List;

public interface RuleEngineService {

    List<DiscountDecision> evaluateAllCoupons(Cart cart);

    Cart applyCouponToCart(Long couponId, Cart cart);

    /**
     * Invalidate cached compiled artifact for a coupon.
     * Should be called when coupon metadata or template changes.
     */
    void invalidateCacheForCoupon(Long couponId);
}
