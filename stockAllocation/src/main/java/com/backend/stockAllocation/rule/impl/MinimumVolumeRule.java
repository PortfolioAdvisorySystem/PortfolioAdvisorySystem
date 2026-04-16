package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;

// ── MinimumVolumeRule ──────────────────────────────────────────────────────────
public class MinimumVolumeRule extends BaseRule {
    public MinimumVolumeRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        BigDecimal minVolume = ruleEntity.getThreshold();
        BigDecimal stockVolume = ctx.getStock().getAvgVolume();
        if (stockVolume.compareTo(minVolume) < 0) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " volume " + stockVolume +
                            " < minimum required " + minVolume);
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
