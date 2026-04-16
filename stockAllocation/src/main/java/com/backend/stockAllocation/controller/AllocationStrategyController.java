package com.backend.stockAllocation.controller;


import com.backend.stockAllocation.dto.request.AllocationStrategyRequest;
import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
import com.backend.stockAllocation.repository.AllocationStrategyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class AllocationStrategyController {

    private final AllocationStrategyRepository strategyRepository;

    @PostMapping
    public ResponseEntity<AllocationStrategy> create(@Valid @RequestBody AllocationStrategyRequest req) {
        AllocationStrategy strategy = AllocationStrategy.builder()
                .name(req.getName())
                .riskProfile(req.getRiskProfile())
                .maxStockConcentration(req.getMaxStockConcentration())
                .maxSectorExposure(req.getMaxSectorExposure())
                .cashBufferPercent(req.getCashBufferPercent())
                .isActive(true)
                .build();
        return ResponseEntity.ok(strategyRepository.save(strategy));
    }

    @GetMapping
    public ResponseEntity<List<AllocationStrategy>> getAll() {
        return ResponseEntity.ok(strategyRepository.findByIsActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationStrategy> getById(@PathVariable Long id) {
        return ResponseEntity.ok(strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AllocationStrategy", id)));
    }

    @GetMapping("/profile/{profile}")
    public ResponseEntity<List<AllocationStrategy>> getByProfile(@PathVariable RiskProfile profile) {
        return ResponseEntity.ok(strategyRepository.findByRiskProfileAndIsActiveTrue(profile));
    }
}
