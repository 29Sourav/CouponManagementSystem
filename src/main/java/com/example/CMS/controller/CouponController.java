package com.example.CMS.controller;

import com.example.CMS.entity.Coupon;
import com.example.CMS.model.dto.CreateCouponRequest;
import com.example.CMS.model.dto.CouponResponse;
import com.example.CMS.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // Create a new coupon
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CreateCouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    // Fetch all coupons
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // Fetch a coupon by ID
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    // Update a coupon
    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @RequestBody CreateCouponRequest request) {

        return ResponseEntity.ok(couponService.updateCoupon(id, request));
    }

    // Delete a coupon
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok("Coupon deleted successfully.");
    }
}
