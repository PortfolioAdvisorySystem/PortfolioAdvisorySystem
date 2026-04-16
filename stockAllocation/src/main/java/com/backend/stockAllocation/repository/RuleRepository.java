package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByIsActiveTrueOrderByPriorityDesc();
    List<Rule> findByRuleTypeAndIsActiveTrue(RuleType ruleType);

    @Query("SELECT r FROM Rule r WHERE r.isActive = true " +
            "AND (r.effectiveDate IS NULL OR r.effectiveDate <= :today) " +
            "AND (r.expiryDate IS NULL OR r.expiryDate >= :today) " +
            "ORDER BY r.priority DESC")
    List<Rule> findCurrentlyActiveRules(@Param("today") LocalDate today);
}
