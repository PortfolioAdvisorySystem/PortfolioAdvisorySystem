package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

public class MinimumLiquidityRule extends BaseRule {
    public MinimumLiquidityRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        int minScore = ruleEntity.getThreshold().intValue();
        Integer score = ctx.getStock().getLiquidityScore();
        if (score == null || score < minScore) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " liquidity score " + score +
                            " < minimum " + minScore);
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
