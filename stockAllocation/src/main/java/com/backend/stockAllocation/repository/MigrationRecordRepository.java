package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.MigrationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigrationRecordRepository extends JpaRepository<MigrationRecord, Long> {
    List<MigrationRecord> findBySubscriberId(Long subscriberId);
    List<MigrationRecord> findBySourceStockId(Long stockId);
    List<MigrationRecord> findByTargetStockId(Long stockId);
}
