package com.backend.stockAllocation.controller;

import com.backend.stockAllocation.dto.request.StockRequest;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.mapper.dto.StockMapper;
import com.backend.stockAllocation.mapper.dto.response.StockResponseDto;
import com.backend.stockAllocation.service.impl.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    @PostMapping
    public ResponseEntity<StockResponseDto> create(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockResponseDto> update(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.update(id,request));
    }

    @GetMapping
    public ResponseEntity<List<StockResponseDto>> getAll() {
        return ResponseEntity.ok(stockService.getAll());
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<StockResponseDto>> getEligible() {
        return ResponseEntity.ok(stockService.getEligible());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockResponseDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(stockService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StockResponseDto> deactivate(@PathVariable Long id,
                                            @RequestParam(defaultValue = "Manual deactivation") String reason) {
        return ResponseEntity.ok(stockService.deactivate(id, reason));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<StockResponseDto> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.suspend(id));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<StockResponseDto> unsuspend(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.unsuspend(id));
    }

    @PostMapping("/{id}/blacklist")
    public ResponseEntity<StockResponseDto> blacklist(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.blacklist(id));
    }
}
