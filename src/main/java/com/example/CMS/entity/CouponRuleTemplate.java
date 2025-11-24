package com.example.CMS.entity;

import com.example.CMS.entity.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coupon_rule_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRuleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    @Column(columnDefinition = "TEXT")
    private String drlTemplate;
}
