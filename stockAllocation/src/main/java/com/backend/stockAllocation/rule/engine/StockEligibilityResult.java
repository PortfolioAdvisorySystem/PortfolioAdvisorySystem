package com.backend.stockAllocation.rule.engine;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StockEligibilityResult {
    private final boolean eligible;
    private final List<RuleEvaluationResult> results;

    public String getSummary() {
        long failed = results.stream().filter(r -> !r.isPassed()).count();
        if (failed == 0) return "All rules passed";
        return failed + " rule(s) failed: " +
                results.stream().filter(r -> !r.isPassed())
                        .map(RuleEvaluationResult::getReason)
                        .reduce((a, b) -> a + "; " + b).orElse("");
    }
}

