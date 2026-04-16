package com.backend.stockAllocation.controller;

import com.backend.stockAllocation.entity.AllocationDecision;
import com.backend.stockAllocation.enums.AllocationRunType;
import com.backend.stockAllocation.service.impl.AllocationEngine;
import com.backend.stockAllocation.service.impl.RebalanceManager;
import com.backend.stockAllocation.service.impl.SubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocation")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationEngine allocationEngine;
    private final RebalanceManager rebalanceManager;
    private final SubscriberService subscriberService;

    @PostMapping("/run/{subscriberId}")
    public ResponseEntity<List<AllocationDecision>> runForSubscriber(@PathVariable Long subscriberId, @RequestParam(defaultValue = "REBALANCE") AllocationRunType runType) {
        var subscriber = subscriberService.getById(subscriberId);
        return ResponseEntity.ok(allocationEngine.allocate(subscriber, null, runType));
    }

    @PostMapping("/rebalance/scheduled")
    public ResponseEntity<Object> scheduledRebalance() {
        return ResponseEntity.ok(rebalanceManager.runScheduledRebalance());
    }

    @PostMapping("/rebalance/event")
    public ResponseEntity<Object> eventRebalance(@RequestParam String reason, @RequestParam(defaultValue = "admin") String triggeredBy) {
        return ResponseEntity.ok(rebalanceManager.triggerEventRebalance(reason, triggeredBy));
    }

    @PostMapping("/rebalance/all")
    public ResponseEntity<String> rebalanceAll(@RequestParam(defaultValue = "REBALANCE") AllocationRunType runType) {
        rebalanceManager.triggerAllocationRun(runType);
        return ResponseEntity.ok("Allocation run triggered for all active subscribers");
    }

    @GetMapping("/rebalance/simulate")
    public ResponseEntity<Map<String, Object>> simulate() {
        return ResponseEntity.ok(rebalanceManager.simulateRebalance());
    }
}
