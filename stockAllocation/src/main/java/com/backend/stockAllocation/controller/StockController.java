package com.backend.stockAllocation.controller;

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
    public ResponseEntity<Stock> create(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stock> update(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.update(id, request));
    }

    @GetMapping
    public ResponseEntity<List<Stock>> getAll() {
        return ResponseEntity.ok(stockService.getAll());
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<Stock>> getEligible() {
        return ResponseEntity.ok(stockService.getEligible());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Stock> deactivate(@PathVariable Long id,
                                            @RequestParam(defaultValue = "Manual deactivation") String reason) {
        return ResponseEntity.ok(stockService.deactivate(id, reason));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Stock> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.suspend(id));
    }

    @PostMapping("/{id}/unsuspend")
    public ResponseEntity<Stock> unsuspend(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.unsuspend(id));
    }

    @PostMapping("/{id}/blacklist")
    public ResponseEntity<Stock> blacklist(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.blacklist(id));
    }
}
