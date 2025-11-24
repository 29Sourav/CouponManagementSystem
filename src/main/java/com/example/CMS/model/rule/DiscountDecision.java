package com.example.CMS.model.rule;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDecision {

    private Long couponId;

    private boolean applicable;

    private double discountAmount;

    private String message; // explanation for UI
}
