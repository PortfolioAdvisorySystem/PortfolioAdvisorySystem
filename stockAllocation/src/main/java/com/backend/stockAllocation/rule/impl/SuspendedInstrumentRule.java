package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

public class SuspendedInstrumentRule extends BaseRule {
    public SuspendedInstrumentRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        if (ctx.getStock().isSuspended()) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " is suspended");
        }
        if (!ctx.getStock().isActive()) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " is inactive");
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
