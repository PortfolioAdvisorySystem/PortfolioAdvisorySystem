package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.enums.AllocationRunType;
import com.backend.stockAllocation.enums.SubscriberStatus;
import com.backend.stockAllocation.repository.RebalanceEventRepository;
import com.backend.stockAllocation.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RebalanceManager {

    private final SubscriberRepository subscriberRepository;
    private final DeallocationEngine deallocationEngine;
    private final MigrationManager migrationManager;
    private final AllocationEngine allocationEngine;
    private final RebalanceEventRepository rebalanceEventRepository;
    private final AuditService auditService;

    /**
     * Full scheduled rebalance:
     * 1. Detect ineligible positions and mark them.
     * 2. For each marked position, find replacements.
     * 3. Execute migration.
     * 4. Re-run allocation for any unallocated pools.
     */
    @Transactional
    public RebalanceEvent runScheduledRebalance() {
        log.info("Starting scheduled rebalance");
        RebalanceEvent event = RebalanceEvent.builder()
                .triggerReason("SCHEDULED")
                .triggeredBy("SCHEDULER")
                .status("IN_PROGRESS")
                .build();
        rebalanceEventRepository.save(event);

        try {
            List<Position> toDealloc = deallocationEngine.detectAndMarkForDeallocation();
            int migrated = executeReplacementsForPositions(toDealloc);

            event.setStatus("COMPLETED");
            event.setCompletedAt(LocalDateTime.now());
            event.setSummary("Deallocated: " + toDealloc.size() + ", Migrated: " + migrated);
            rebalanceEventRepository.save(event);

            auditService.log("REBALANCE_COMPLETED",
                    "Scheduled rebalance done. Deallocated=" + toDealloc.size() + " Migrated=" + migrated,
                    "SCHEDULER", null, "System");

            log.info("Scheduled rebalance complete. Deallocated={} Migrated={}", toDealloc.size(), migrated);
        } catch (Exception e) {
            event.setStatus("FAILED");
            event.setSummary("Error: " + e.getMessage());
            rebalanceEventRepository.save(event);
            log.error("Rebalance failed: {}", e.getMessage(), e);
            throw e;
        }
        return event;
    }

    /**
     * Event-driven rebalance triggered by a specific cause (e.g. rule change, stock deactivation).
     */
    @Transactional
    public RebalanceEvent triggerEventRebalance(String reason, String triggeredBy) {
        log.info("Event-driven rebalance triggered: {} by {}", reason, triggeredBy);

        RebalanceEvent event = RebalanceEvent.builder()
                .triggerReason(reason)
                .triggeredBy(triggeredBy)
                .status("IN_PROGRESS")
                .build();
        rebalanceEventRepository.save(event);

        List<Position> toDealloc = deallocationEngine.detectAndMarkForDeallocation();
        int migrated = executeReplacementsForPositions(toDealloc);

        event.setStatus("COMPLETED");
        event.setCompletedAt(LocalDateTime.now());
        event.setSummary("Reason: " + reason + " | Deallocated: " + toDealloc.size() + " | Migrated: " + migrated);
        rebalanceEventRepository.save(event);

        return event;
    }

    /**
     * Trigger a full allocation run for all ACTIVE subscribers.
     */
    @Transactional
    public void triggerAllocationRun(AllocationRunType runType) {
        List<Subscriber> active = subscriberRepository.findByStatus(SubscriberStatus.ACTIVE);
        log.info("Triggering {} allocation run for {} subscribers", runType, active.size());
        for (Subscriber sub : active) {
            try {
                allocationEngine.allocate(sub, null, runType);
            } catch (Exception e) {
                log.error("Allocation failed for subscriber {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    /**
     * Simulate rebalance without persisting — returns summary.
     */
    public Map<String, Object> simulateRebalance() {
        List<Position> wouldDealloc = deallocationEngine.getPositionsMarkedForDeallocation();
        Map<String, Object> simulation = new LinkedHashMap<>();
        simulation.put("positionsToDealloc", wouldDealloc.size());
        simulation.put("affectedSubscribers",
                wouldDealloc.stream()
                        .map(p -> p.getPortfolio().getSubscriber().getId())
                        .collect(Collectors.toSet()).size());
        simulation.put("affectedStocks",
                wouldDealloc.stream()
                        .map(p -> p.getStock().getSymbol())
                        .collect(Collectors.toSet()));
        return simulation;
    }

    private int executeReplacementsForPositions(List<Position> toDealloc) {
        if (toDealloc.isEmpty()) return 0;

        // Group by source stock
        Map<Stock, List<Position>> byStock = toDealloc.stream()
                .collect(Collectors.groupingBy(Position::getStock));

        Map<Stock, List<Stock>> replacementMap = new LinkedHashMap<>();

        for (Stock sourceStock : byStock.keySet()) {
            List<Position> positions = byStock.get(sourceStock);
            if (positions.isEmpty()) continue;
            Position sample = positions.get(0);
            Portfolio portfolio = sample.getPortfolio();
            Subscriber subscriber = portfolio.getSubscriber();

            List<Stock> replacements = migrationManager.findReplacements(
                    sourceStock, subscriber, sample.getStrategy(), portfolio);

            // Take top 3 replacements for one-to-many redistribution
            replacementMap.put(sourceStock, replacements.stream().limit(3).collect(Collectors.toList()));
        }

        List<MigrationRecord> records = migrationManager.executeMigration(toDealloc, replacementMap);
        return records.size();
    }
}
