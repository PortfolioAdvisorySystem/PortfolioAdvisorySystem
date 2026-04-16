package com.backend.stockAllocation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {
    private final ApprovalWorkflowService workflowService;

    @PostMapping("/submit")
    public ResponseEntity<ActionCommand> submit(@RequestParam ActionType actionType, @RequestBody Map<String, Object> payload, @RequestParam(defaultValue = "maker") String submittedBy) {
        return ResponseEntity.ok(workflowService.submitCommand(actionType, payload, submittedBy));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ActionCommand> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(workflowService.reviewCommand(
                id, request.getReviewedBy(), request.isApproved(), request.getNote()));
    }

    @PostMapping("/emergency-override")
    public ResponseEntity<ActionCommand> emergencyOverride(@RequestParam ActionType actionType, @RequestBody Map<String, Object> payload, @RequestParam String authorizedBy) {
        return ResponseEntity.ok(workflowService.emergencyOverride(actionType, payload, authorizedBy));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ActionCommand>> getPending() {
        return ResponseEntity.ok(workflowService.getPendingCommands());
    }

    @GetMapping
    public ResponseEntity<List<ActionCommand>> getAll() {
        return ResponseEntity.ok(workflowService.getAllCommands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionCommand> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getCommand(id));
    }
}
