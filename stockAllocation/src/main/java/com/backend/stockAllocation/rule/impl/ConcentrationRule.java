package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ConcentrationRule extends BaseRule {
    public ConcentrationRule(Rule rule) { super(rule); }
    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        BigDecimal maxConcentration = ruleEntity.getThreshold();
        BigDecimal totalAUM = ctx.getTotalAUM();
        if (totalAUM == null || totalAUM.compareTo(BigDecimal.ZERO) == 0) {
            return RuleEvaluationResult.pass(getRuleName());
        }
        BigDecimal currentWeight = ctx.getCurrentStockWeight() != null ? ctx.getCurrentStockWeight() : BigDecimal.ZERO;
        BigDecimal proposed = ctx.getProposedAmount() != null ? ctx.getProposedAmount() : BigDecimal.ZERO;
        BigDecimal newPct = currentWeight.add(proposed)
                .divide(totalAUM, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (newPct.compareTo(maxConcentration) > 0) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " concentration " +
                            newPct.setScale(2, RoundingMode.HALF_UP) + "% exceeds max " + maxConcentration + "%");
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
