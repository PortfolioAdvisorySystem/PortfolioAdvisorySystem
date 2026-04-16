package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SectorCapRule extends BaseRule {
    public SectorCapRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        String targetSector = ruleEntity.getTargetSector();
        if (targetSector != null && !targetSector.equalsIgnoreCase(ctx.getStock().getSector())) {
            return RuleEvaluationResult.pass(getRuleName());
        }
        BigDecimal maxSectorPct = ruleEntity.getThreshold();
        BigDecimal totalAUM = ctx.getTotalAUM();
        if (totalAUM == null || totalAUM.compareTo(BigDecimal.ZERO) == 0) {
            return RuleEvaluationResult.pass(getRuleName());
        }
        BigDecimal sectorWeight = ctx.getCurrentSectorWeight() != null ? ctx.getCurrentSectorWeight() : BigDecimal.ZERO;
        BigDecimal proposed = ctx.getProposedAmount() != null ? ctx.getProposedAmount() : BigDecimal.ZERO;
        BigDecimal newSectorPct = sectorWeight.add(proposed)
                .divide(totalAUM, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (newSectorPct.compareTo(maxSectorPct) > 0) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Sector " + ctx.getStock().getSector() + " exposure " + newSectorPct.setScale(2, RoundingMode.HALF_UP) +
                            "% would exceed cap of " + maxSectorPct + "%");
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
