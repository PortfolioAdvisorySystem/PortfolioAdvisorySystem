package com.backend.stockAllocation.controller;


import com.backend.stockAllocation.dto.request.AllocationStrategyRequest;
import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
import com.backend.stockAllocation.mapper.dto.AllocationStrategyMapper;
import com.backend.stockAllocation.mapper.dto.response.AllocationStrategyResponseDTO;
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
    private final AllocationStrategyMapper allocationStrategyMapper;
    @PostMapping
    public ResponseEntity<AllocationStrategyResponseDTO> create(@Valid @RequestBody AllocationStrategyRequest req) {
        AllocationStrategy strategy = AllocationStrategy.builder()
                .name(req.getName())
                .riskProfile(req.getRiskProfile())
                .maxStockConcentration(req.getMaxStockConcentration())
                .maxSectorExposure(req.getMaxSectorExposure())
                .cashBufferPercent(req.getCashBufferPercent())
                .isActive(true)
                .build();
        return ResponseEntity.ok(allocationStrategyMapper.toResponseDTO(strategyRepository.save(strategy)));
    }

    @GetMapping
    public ResponseEntity<List<AllocationStrategyResponseDTO>> getAll() {
        return ResponseEntity.ok(allocationStrategyMapper.toResponseDTOList(strategyRepository.findByIsActiveTrue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationStrategyResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(allocationStrategyMapper.toResponseDTO(strategyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AllocationStrategy", id))));
    }

    @GetMapping("/profile/{profile}")
    public ResponseEntity<List<AllocationStrategyResponseDTO>> getByProfile(@PathVariable RiskProfile profile) {
        return ResponseEntity.ok(allocationStrategyMapper.toResponseDTOList(strategyRepository.findByRiskProfileAndIsActiveTrue(profile)));
    }
}
