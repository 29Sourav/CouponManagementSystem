package com.example.CMS.repository;

import com.example.CMS.entity.CouponRuleTemplate;
import com.example.CMS.entity.enums.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRuleTemplateRepository extends JpaRepository<CouponRuleTemplate, Long> {

    Optional<CouponRuleTemplate> findByType(CouponType type);
}
