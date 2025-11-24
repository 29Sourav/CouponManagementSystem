package com.example.CMS.service.impl;

import com.example.CMS.entity.Coupon;
import com.example.CMS.entity.CouponRuleTemplate;
import com.example.CMS.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    @Override
    public String generateDRL(Coupon coupon, CouponRuleTemplate template) {

        String drl = template.getDrlTemplate();

        Map<String, Object> metadata = coupon.getMetadata();

        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {

                String key = entry.getKey();
                Object value = entry.getValue();
                String placeholder = "{{" + key + "}}";

                if (value instanceof List<?> listValue) {
                    // Convert list → "A","B","C"
                    StringBuilder sb = new StringBuilder();
                    for (Object item : listValue) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append("\"").append(item.toString()).append("\"");
                    }
                    drl = drl.replace(placeholder, sb.toString());

                } else if (value instanceof String) {
                    drl = drl.replace(placeholder, "\"" + value + "\"");

                } else {
                    drl = drl.replace(placeholder, value.toString());
                }
            }
        }

        // Replace coupon ID
        drl = drl.replace("{{couponId}}", coupon.getId().toString());

        return drl;
    }
}
