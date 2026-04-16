package com.backend.stockAllocation.rule.engine;
import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class RuleEvaluationResult {
    private final boolean passed;
    private final String ruleName;
    private final String reason;
    public static RuleEvaluationResult pass(String ruleName) {
        return RuleEvaluationResult.builder().passed(true).ruleName(ruleName).reason("OK").build();
    }
    public static RuleEvaluationResult fail(String ruleName, String reason) {
        return RuleEvaluationResult.builder().passed(false).ruleName(ruleName).reason(reason).build();
    }
}

