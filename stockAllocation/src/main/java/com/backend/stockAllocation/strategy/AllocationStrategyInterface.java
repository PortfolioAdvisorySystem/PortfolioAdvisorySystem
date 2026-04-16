package com.backend.stockAllocation.strategy;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.entity.Subscriber;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy Pattern: each concrete strategy knows how to distribute
 * a given investable amount across an eligible stock list.
 */
public interface AllocationStrategyInterface {
    ProposedAllocation generateInitialAllocation(
            Subscriber subscriber,
            BigDecimal investableAmount,
            List<Stock> eligibleStocks,
            AllocationStrategy strategyConfig);
}
