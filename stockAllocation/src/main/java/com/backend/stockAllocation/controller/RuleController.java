package com.backend.stockAllocation.controller;

import com.backend.stockAllocation.dto.request.RuleRequest;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.service.impl.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {
    private final RuleService ruleService;

    @PostMapping
    public ResponseEntity<Rule> create(@Valid @RequestBody RuleRequest request,
                                       @RequestParam(defaultValue = "admin") String createdBy) {
        return ResponseEntity.ok(ruleService.createRule(request, createdBy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> update(@PathVariable Long id, @Valid @RequestBody RuleRequest request, @RequestParam(defaultValue = "admin") String updatedBy) {
        return ResponseEntity.ok(ruleService.updateRule(id, request, updatedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, @RequestParam(defaultValue = "admin") String deactivatedBy) {
        ruleService.deactivateRule(id, deactivatedBy);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Rule>> getAll() {

        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Rule>> getActive() {
        return ResponseEntity.ok(ruleService.getActiveRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> getById(@PathVariable Long id) {

        return ResponseEntity.ok(ruleService.getById(id));
    }
}
