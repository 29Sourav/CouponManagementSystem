package com.example.CMS.service;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.dto.CreateCouponRequest;
import com.example.CMS.model.dto.CouponResponse;
import com.example.CMS.model.rule.DiscountDecision;

import java.util.List;

public interface CouponService {

    CouponResponse createCoupon(CreateCouponRequest request);

    List<CouponResponse> getAllCoupons();

    CouponResponse getCouponById(Long id);

    CouponResponse updateCoupon(Long id, CreateCouponRequest request);

    void deleteCoupon(Long id);

    List<DiscountDecision> getApplicableCoupons(Cart cart);

    Cart applyCoupon(Long id, Cart cart);
}
