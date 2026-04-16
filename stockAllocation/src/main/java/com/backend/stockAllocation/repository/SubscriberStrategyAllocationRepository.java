package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.SubscriberStrategyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberStrategyAllocationRepository extends JpaRepository<SubscriberStrategyAllocation, Long> {
    List<SubscriberStrategyAllocation> findBySubscriberId(Long subscriberId);
    Optional<SubscriberStrategyAllocation> findBySubscriberIdAndStrategyId(Long subscriberId, Long strategyId);
    void deleteBySubscriberId(Long subscriberId);
}
