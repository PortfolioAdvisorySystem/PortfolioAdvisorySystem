package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.RebalanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RebalanceEventRepository extends JpaRepository<RebalanceEvent, Long> {
    List<RebalanceEvent> findByOrderByTriggeredAtDesc();
}
