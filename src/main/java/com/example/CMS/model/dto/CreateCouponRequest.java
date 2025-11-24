package com.example.CMS.model.dto;

import com.example.CMS.entity.enums.CouponType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponRequest {

    private String code;

    private CouponType type;   // CART, PRODUCT, BXGY

    private Boolean active;

    private String startDate;  // ISO string (yyyy-MM-dd)

    private String endDate;

    private Map<String, Object> metadata;   // JSON string

    private Long ruleTemplateId; // FK to coupon_rule_template
}
