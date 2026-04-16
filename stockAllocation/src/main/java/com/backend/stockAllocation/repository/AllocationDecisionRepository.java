package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.AllocationDecision;
import com.backend.stockAllocation.enums.AllocationRunType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationDecisionRepository extends JpaRepository<AllocationDecision, Long> {
    List<AllocationDecision> findBySubscriberId(Long subscriberId);
    List<AllocationDecision> findByRunId(String runId);
    List<AllocationDecision> findByRunType(AllocationRunType runType);
}
