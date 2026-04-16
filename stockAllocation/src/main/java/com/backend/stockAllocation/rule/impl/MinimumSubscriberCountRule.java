package com.backend.stockAllocation.rule.impl;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

public class MinimumSubscriberCountRule extends BaseRule {
    public MinimumSubscriberCountRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        long minCount = ruleEntity.getThreshold().longValue();
        long actual = ctx.getSubscriberCountForStock();
        // For initial allocation, count = 0 is expected. We check that at least 0 subscribers
        // already hold it — this rule is mainly for deallocation checks.
        // If actual < minCount and actual > 0 it means existing holders are below minimum.
        if (actual > 0 && actual < minCount) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock " + ctx.getStock().getSymbol() + " has only " + actual +
                            " subscribers, minimum required is " + minCount);
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}
