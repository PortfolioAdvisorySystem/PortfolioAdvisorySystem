package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.ActionCommand;
import com.backend.stockAllocation.enums.ActionType;
import com.backend.stockAllocation.enums.ApprovalStatus;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
import com.backend.stockAllocation.exception.WorkflowException;
import com.backend.stockAllocation.repository.ActionCommandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalWorkflowService {
    private final ActionCommandRepository actionCommandRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;


     // Submit a high-impact command for approval.

    @Transactional
    public ActionCommand submitCommand(ActionType actionType, Object payload, String submittedBy) {
        String stateJson;
        stateJson = objectMapper.writeValueAsString(payload);

        ActionCommand command = ActionCommand.builder()
                .actionType(actionType)
                .state(stateJson)
                .submittedBy(submittedBy)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();
        actionCommandRepository.save(command);

        auditService.log("COMMAND_SUBMITTED",
                "ActionType=" + actionType + " by " + submittedBy,
                submittedBy, command.getId(), "ActionCommand");

        log.info("Command {} submitted by {} for approval", actionType, submittedBy);
        return command;
    }


     // Reviewer approves or rejects a pending command.

    @Transactional
    public ActionCommand reviewCommand(Long commandId, String reviewedBy,
                                       boolean approved, String note) {
        ActionCommand command = actionCommandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("ActionCommand", commandId));

        if (command.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new WorkflowException("Command " + commandId + " is already " + command.getApprovalStatus());
        }

        command.setReviewedBy(reviewedBy);
        command.setReviewNote(note);
        command.setReviewedAt(LocalDateTime.now());
        command.setApprovalStatus(approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        actionCommandRepository.save(command);

        auditService.log(approved ? "COMMAND_APPROVED" : "COMMAND_REJECTED",
                "CommandId=" + commandId + " by " + reviewedBy + " note=" + note,
                reviewedBy, commandId, "ActionCommand");

        return command;
    }


     //Emergency override — bypasses normal approval for authorized users.

    @Transactional
    public ActionCommand emergencyOverride(ActionType actionType, Object payload, String authorizedBy) {
        String stateJson;
        stateJson = objectMapper.writeValueAsString(payload);

        ActionCommand command = ActionCommand.builder()
                .actionType(actionType)
                .state(stateJson)
                .submittedBy(authorizedBy)
                .reviewedBy(authorizedBy)
                .approvalStatus(ApprovalStatus.APPROVED)
                .emergencyOverride(true)
                .reviewedAt(LocalDateTime.now())
                .reviewNote("EMERGENCY OVERRIDE by " + authorizedBy)
                .build();
        actionCommandRepository.save(command);

        auditService.log("EMERGENCY_OVERRIDE",
                "ActionType=" + actionType + " authorized by " + authorizedBy,
                authorizedBy, command.getId(), "ActionCommand");

        log.warn("EMERGENCY OVERRIDE used: {} by {}", actionType, authorizedBy);
        return command;
    }

    public List<ActionCommand> getPendingCommands() {
        return actionCommandRepository.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    public List<ActionCommand> getAllCommands() {
        return actionCommandRepository.findAll();
    }

    public ActionCommand getCommand(Long id) {
        return actionCommandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionCommand", id));
    }
}
