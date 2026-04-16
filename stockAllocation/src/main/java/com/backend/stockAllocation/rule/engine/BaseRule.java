package com.backend.stockAllocation.rule.engine;
import com.backend.stockAllocation.entity.Rule;

/**
 * Specification Pattern: each rule is a self-contained evaluator.
 * Rules are loaded from the DB and mapped to implementations at runtime.
 */
public abstract class BaseRule {
    protected final Rule ruleEntity;
    protected BaseRule(Rule ruleEntity) {
        this.ruleEntity = ruleEntity;
    }
    /**
     * Evaluate whether the stock in the given context passes this rule.
     */
    public abstract RuleEvaluationResult evaluate(RuleEvaluationContext context);
    public String getRuleName() {
        return ruleEntity.getName();
    }
    public int getPriority() {
        return ruleEntity.getPriority();
    }
}

