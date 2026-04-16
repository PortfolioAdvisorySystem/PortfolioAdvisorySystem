package com.backend.stockAllocation.rule.impl;

import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.enums.RiskCategory;
import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.rule.engine.BaseRule;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluationResult;

public class RiskProfileCompatibilityRule extends BaseRule {
    public RiskProfileCompatibilityRule(Rule rule) { super(rule); }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext ctx) {
        RiskProfile subscriberProfile = ctx.getSubscriber().getPrimaryRiskProfile();
        RiskCategory stockCategory = ctx.getStock().getRiskCategory();
        if (!isCompatible(subscriberProfile, stockCategory)) {
            return RuleEvaluationResult.fail(getRuleName(),
                    "Stock risk " + stockCategory + " is incompatible with subscriber profile " + subscriberProfile);
        }
        return RuleEvaluationResult.pass(getRuleName());
    }

    private boolean isCompatible(RiskProfile profile, RiskCategory category) {
        return switch (profile) {
            case CONSERVATIVE -> category == RiskCategory.LOW;
            case MODERATE     -> category == RiskCategory.LOW || category == RiskCategory.MEDIUM;
            case AGGRESSIVE   -> true; // Aggressive can hold any risk category
        };
    }
}

