package com.example.CMS.controller;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.dto.ApplicableCouponsRequest;
import com.example.CMS.model.rule.DiscountDecision;
import com.example.CMS.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CouponService couponService;

    // Fetch all applicable coupons for a given cart
    @PostMapping("/applicable-coupons")
    public ResponseEntity<List<DiscountDecision>> getApplicableCoupons(
            @RequestBody ApplicableCouponsRequest request) {

        return ResponseEntity.ok(couponService.getApplicableCoupons(request.getCart()));
    }

    // Apply a specific coupon to a cart and return updated cart
    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<Map<String, Object>> applyCouponToCart(
            @PathVariable Long id,
            @RequestBody Map<String, Cart> payload) {

        Cart cart = payload.get("cart");

        Cart updatedCart = couponService.applyCoupon(id, cart);
        Map<String, Object> response = new HashMap<>();
        response.put("updated_cart", updatedCart);
        return ResponseEntity.ok(response);
    }
}
