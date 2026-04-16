package com.backend.stockAllocation.rule.engine;

import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core rule engine: loads active rules, builds implementations, and evaluates them
 * against a given context. Returns a StockEligibilityResult with per-rule details.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluator {

    private final RuleRepository ruleRepository;
    private final RuleFactory ruleFactory;

    /**
     * Checks all currently active and effective rules for the given context.
     */
    public StockEligibilityResult checkStockEligibility(RuleEvaluationContext context) {
        List<Rule> activeRules = ruleRepository.findCurrentlyActiveRules(LocalDate.now());
        return evaluateRules(activeRules, context);
    }

    /**
     * Evaluate a specific subset of rules (e.g. for deallocation checks).
     */
    public StockEligibilityResult checkWithRules(List<Rule> rules, RuleEvaluationContext context) {
        return evaluateRules(rules, context);
    }

    private StockEligibilityResult evaluateRules(List<Rule> rules, RuleEvaluationContext context) {
        List<RuleEvaluationResult> results = new ArrayList<>();
        boolean allPassed = true;

        for (Rule rule : rules) {
            // Skip rules targeting a different risk profile
            if (rule.getTargetRiskProfile() != null && context.getSubscriber() != null) {
                String profileName = context.getSubscriber().getPrimaryRiskProfile().name();
                if (!rule.getTargetRiskProfile().equalsIgnoreCase(profileName)) {
                    continue;
                }
            }

            try {
                BaseRule ruleImpl = ruleFactory.create(rule);
                RuleEvaluationResult result = ruleImpl.evaluate(context);
                results.add(result);
                if (!result.isPassed()) {
                    allPassed = false;
                    log.debug("Rule '{}' FAILED for stock '{}': {}",
                            rule.getName(), context.getStock().getSymbol(), result.getReason());
                }
            } catch (Exception e) {
                log.error("Error evaluating rule '{}': {}", rule.getName(), e.getMessage());
                results.add(RuleEvaluationResult.fail(rule.getName(), "Rule evaluation error: " + e.getMessage()));
                allPassed = false;
            }
        }

        return StockEligibilityResult.builder()
                .eligible(allPassed)
                .results(results)
                .build();
    }

    /**
     * Check a stock universe and return only eligible ones.
     */
    public List<Stock> filterEligibleStocks(List<Stock> stocks, RuleEvaluationContext contextTemplate) {
        List<Rule> activeRules = ruleRepository.findCurrentlyActiveRules(LocalDate.now());
        List<Stock> eligible = new ArrayList<>();

        for (Stock stock : stocks) {
            RuleEvaluationContext ctx = RuleEvaluationContext.builder()
                    .subscriber(contextTemplate.getSubscriber())
                    .stock(stock)
                    .strategy(contextTemplate.getStrategy())
                    .portfolio(contextTemplate.getPortfolio())
                    .currentStockWeight(contextTemplate.getCurrentStockWeight())
                    .currentSectorWeight(contextTemplate.getCurrentSectorWeight())
                    .totalAllocated(contextTemplate.getTotalAllocated())
                    .totalAUM(contextTemplate.getTotalAUM())
                    .subscriberCountForStock(contextTemplate.getSubscriberCountForStock())
                    .stockMetrics(contextTemplate.getStockMetrics())
                    .proposedAmount(contextTemplate.getProposedAmount())
                    .build();

            StockEligibilityResult result = evaluateRules(activeRules, ctx);
            if (result.isEligible()) {
                eligible.add(stock);
            }
        }
        return eligible;
    }
}
