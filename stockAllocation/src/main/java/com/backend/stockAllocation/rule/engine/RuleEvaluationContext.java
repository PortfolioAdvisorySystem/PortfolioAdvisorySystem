package com.backend.stockAllocation.rule.engine;
import com.backend.stockAllocation.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Immutable context object passed to each rule during evaluation.
 * Carries all data needed so rules remain pure/stateless.
 */
@Getter
@Builder
public class RuleEvaluationContext {
    private final Subscriber subscriber;
    private final Stock stock;
    private final AllocationStrategy strategy;
    private final Portfolio portfolio;

    /** Current total allocation weight of this stock across the portfolio */
    private final BigDecimal currentStockWeight;

    /** Current total allocation weight for the stock's sector in portfolio */
    private final BigDecimal currentSectorWeight;

    /** Total allocated amount in portfolio */
    private final BigDecimal totalAllocated;

    /** Total AUM for subscriber */
    private final BigDecimal totalAUM;

    /** Number of active subscribers holding this stock (for min-subscriber-count rule) */
    private final long subscriberCountForStock;

    /** Stock-level metrics snapshot */
    private final Map<String, BigDecimal> stockMetrics;

    /** Amount being proposed for allocation */
    private final BigDecimal proposedAmount;
}
