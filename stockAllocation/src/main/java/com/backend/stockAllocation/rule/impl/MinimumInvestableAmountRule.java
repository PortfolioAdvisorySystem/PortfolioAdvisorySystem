package com.backend.stockAllocation.rule.impl;

import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

import java.math.BigDecimal;

public class MinimumInvestableAmountRule extends BaseRule {
    public MinimumInvestableAmountRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        BigDecimal minAmount = ruleEntity.getThreshold();
        BigDecimal proposed = ctx.getProposedAmount();
        if (proposed != null && proposed.compareTo(BigDecimal.ZERO) > 0 && proposed.compareTo(minAmount) < 0) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Proposed amount " + proposed + " is below minimum investable amount " + minAmount);
        }
        return RuleEvaluationResult.pass(getRuleName());
    }
}

