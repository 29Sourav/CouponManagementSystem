package com.example.CMS.entity;

import com.example.CMS.entity.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;
import com.example.CMS.common.MapToJsonConverter;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "coupon_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private Boolean active;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "JSON")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_template_id")
    private CouponRuleTemplate ruleTemplate;
}
