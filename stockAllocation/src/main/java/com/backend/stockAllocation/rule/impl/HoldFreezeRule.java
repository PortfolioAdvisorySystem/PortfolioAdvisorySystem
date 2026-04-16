package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

public class HoldFreezeRule extends BaseRule {
    public HoldFreezeRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        String targetSymbol = ruleEntity.getTargetStockSymbol();
        if (targetSymbol != null && targetSymbol.equalsIgnoreCase(ctx.getStock().getSymbol())) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " is under a hold/freeze restriction");
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
