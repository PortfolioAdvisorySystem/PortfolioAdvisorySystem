package com.backend.stockAllocation.rule.impl;

import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

public class BlacklistRule extends BaseRule {
    public BlacklistRule(Rule rule) { super(rule); }
    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        if (ctx.getStock().isBlacklisted()) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " is blacklisted");
        }
        String targetSymbol = ruleEntity.getTargetStockSymbol();
        if (targetSymbol != null && targetSymbol.equalsIgnoreCase(ctx.getStock().getSymbol())) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " is explicitly blacklisted by rule");
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}

