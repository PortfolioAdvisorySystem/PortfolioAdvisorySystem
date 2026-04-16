package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.AuditLog;
import com.backend.stockAllocation.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String details, String performedBy, Long entityId, String entityType) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .details(details)
                .performedBy(performedBy)
                .entityId(entityId)
                .entityType(entityType)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
