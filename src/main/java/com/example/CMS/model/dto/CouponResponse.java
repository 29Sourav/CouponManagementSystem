package com.example.CMS.model.dto;

import com.example.CMS.entity.enums.CouponType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {

    private Long id;

    private String code;

    private CouponType type;

    private Boolean active;

    private String startDate;

    private String endDate;

    private Map<String,Object> metadata;

    private Long ruleTemplateId;
}
