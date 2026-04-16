package com.backend.stockAllocation.controller;

import com.backend.stockAllocation.dto.request.InflowRequest;
import com.backend.stockAllocation.dto.request.SubscriberRequest;
import com.backend.stockAllocation.entity.Subscriber;
import com.backend.stockAllocation.enums.SubscriberStatus;
import com.backend.stockAllocation.service.impl.SubscriberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.stockAllocation.dto.request.StrategyAllocationRequest;
import java.util.List;

@RestController
@RequestMapping("/api/subscriber")
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;
    @PostMapping
    public ResponseEntity<Subscriber> onboard(@Valid @RequestBody SubscriberRequest request) throws BadRequestException {
        return ResponseEntity.ok(subscriberService.onboard(request));
    }
    @PostMapping("/bulk")
    public ResponseEntity<List<Subscriber>> bulkOnboard(@Valid @RequestBody List<SubscriberRequest> requests) {
        return ResponseEntity.ok(subscriberService.bulkOnboard(requests));
    }
    @GetMapping
    public ResponseEntity<List<Subscriber>> getAll() {

        return ResponseEntity.ok(subscriberService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Subscriber> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriberService.getById(id));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Subscriber>> getByStatus(@PathVariable SubscriberStatus status) {
        return ResponseEntity.ok(subscriberService.getByStatus(status));
    }
    @PatchMapping("/{id}/strategy-mix")
    public ResponseEntity<Subscriber> updateStrategyMix(
            @PathVariable Long id,
            @Valid @RequestBody List<StrategyAllocationRequest> newMix) throws BadRequestException {
        return ResponseEntity.ok(subscriberService.updateStrategyMix(id, newMix));
    }
    @PostMapping("/{id}/inflow")
    public ResponseEntity<Subscriber> addInflow(
            @PathVariable Long id,
            @Valid @RequestBody InflowRequest request) {
        return ResponseEntity.ok(subscriberService.addInflow(id, request.getAmount()));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Subscriber> updateStatus(@PathVariable Long id,@RequestParam SubscriberStatus status) {
        return ResponseEntity.ok(subscriberService.updateStatus(id, status));
    }

}
