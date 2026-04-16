package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.enums.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationStrategyRepository extends JpaRepository<AllocationStrategy, Long> {
    List<AllocationStrategy> findByRiskProfileAndIsActiveTrue(RiskProfile riskProfile);
    Optional<AllocationStrategy> findByNameAndIsActiveTrue(String name);
    List<AllocationStrategy> findByIsActiveTrue();
}
