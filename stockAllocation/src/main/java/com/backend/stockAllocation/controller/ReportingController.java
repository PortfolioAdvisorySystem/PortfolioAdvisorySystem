package com.backend.stockAllocation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {
    private final ReportingService reportingService;
    private final AuditService auditService;

    @GetMapping("/allocation/subscriber/{subscriberId}")
    public ResponseEntity<Map<String, Object>> allocationBySubscriber(@PathVariable Long subscriberId) {
        return ResponseEntity.ok(reportingService.getAllocationBySubscriber(subscriberId));
    }

    @GetMapping("/allocation/strategy")
    public ResponseEntity<Map<String, Object>> allocationByStrategy() {
        return ResponseEntity.ok(reportingService.getAllocationByStrategy());
    }

    @GetMapping("/deallocation-queue")
    public ResponseEntity<List<Map<String, Object>>> deallocationQueue() {
        return ResponseEntity.ok(reportingService.getDeallocationQueue());
    }

    @GetMapping("/migration-history")
    public ResponseEntity<List<MigrationRecord>> migrationHistory() {
        return ResponseEntity.ok(reportingService.getMigrationHistory());
    }

    @GetMapping("/migration-history/subscriber/{subscriberId}")
    public ResponseEntity<List<MigrationRecord>> migrationHistoryForSubscriber(@PathVariable Long subscriberId) {
        return ResponseEntity.ok(reportingService.getMigrationHistoryForSubscriber(subscriberId));
    }

    @GetMapping("/rule-breach-summary")
    public ResponseEntity<Map<String, Long>> ruleBreachSummary() {
        return ResponseEntity.ok(reportingService.getRuleBreachSummary());
    }

    @GetMapping("/rebalance-impact/{runId}")
    public ResponseEntity<Map<String, Object>> rebalanceImpact(@PathVariable String runId) {
        return ResponseEntity.ok(reportingService.getRebalanceImpact(runId));
    }

    @GetMapping("/unallocated-pool")
    public ResponseEntity<List<Map<String, Object>>> unallocatedPool() {
        return ResponseEntity.ok(reportingService.getUnallocatedPool());
    }

    @GetMapping("/rebalance-history")
    public ResponseEntity<List<RebalanceEvent>> rebalanceHistory() {
        return ResponseEntity.ok(reportingService.getRebalanceHistory());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/audit-logs/{entityType}/{entityId}")
    public ResponseEntity<?> auditLogsForEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.getLogsForEntity(entityType, entityId));
    }
}
