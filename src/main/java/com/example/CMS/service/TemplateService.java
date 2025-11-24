package com.example.CMS.service;

import com.example.CMS.entity.Coupon;
import com.example.CMS.entity.CouponRuleTemplate;

public interface TemplateService {

    String generateDRL(Coupon coupon, CouponRuleTemplate template);
}
