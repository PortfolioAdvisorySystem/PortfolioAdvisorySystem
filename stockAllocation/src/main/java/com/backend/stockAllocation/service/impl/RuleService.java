package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.dto.request.RuleRequest;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Rule createRule(RuleRequest request, String createdBy) {
        Rule rule = Rule.builder()
                .name(request.getName())
                .ruleType(request.getRuleType())
                .threshold(request.getThreshold())
                .targetStockSymbol(request.getTargetStockSymbol())
                .targetSector(request.getTargetSector())
                .targetRiskProfile(request.getTargetRiskProfile())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .priority(request.getPriority())
                .description(request.getDescription())
                .isActive(true)
                .version(1)
                .build();

        rule.setRuleSnapshot(toJson(rule));
        ruleRepository.save(rule);

        auditService.log("RULE_CREATED", "Rule " + rule.getName() + " created by " + createdBy,
                createdBy, rule.getId(), "Rule");
        return rule;
    }

    @Transactional
    public Rule updateRule(Long id, RuleRequest request, String updatedBy) {
        Rule existing = getById(id);

        // Version the rule: deactivate old, create new version
        existing.setActive(false);
        ruleRepository.save(existing);

        Rule updated = Rule.builder()
                .name(request.getName())
                .ruleType(request.getRuleType())
                .threshold(request.getThreshold())
                .targetStockSymbol(request.getTargetStockSymbol())
                .targetSector(request.getTargetSector())
                .targetRiskProfile(request.getTargetRiskProfile())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .priority(request.getPriority())
                .description(request.getDescription())
                .isActive(true)
                .version(existing.getVersion() + 1)
                .build();

        updated.setRuleSnapshot(toJson(updated));
        ruleRepository.save(updated);

        auditService.log("RULE_UPDATED",
                "Rule " + id + " updated to version " + updated.getVersion() + " by " + updatedBy,
                updatedBy, updated.getId(), "Rule");
        return updated;
    }

    @Transactional
    public void deactivateRule(Long id, String deactivatedBy) {
        Rule rule = getById(id);
        rule.setActive(false);
        ruleRepository.save(rule);
        auditService.log("RULE_DEACTIVATED", "Rule " + id + " deactivated by " + deactivatedBy,
                deactivatedBy, id, "Rule");
    }

    public List<Rule> getActiveRules() {
        return ruleRepository.findCurrentlyActiveRules(LocalDate.now());
    }

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    public Rule getById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule", id));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
