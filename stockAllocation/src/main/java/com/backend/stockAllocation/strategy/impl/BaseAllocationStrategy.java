package com.backend.stockAllocation.strategy.impl;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.entity.Subscriber;
import com.backend.stockAllocation.strategy.AllocationStrategyInterface;
import com.backend.stockAllocation.strategy.ProposedAllocation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Base strategy: distributes investable amount equally across eligible stocks,
 * capped by the strategy's maxStockConcentration.
 * Subclasses may override to apply profile-specific weighting.
 */
@Slf4j
public abstract class BaseAllocationStrategy implements AllocationStrategyInterface {

    @Override
    public ProposedAllocation generateInitialAllocation(
            Subscriber subscriber,
            BigDecimal investableAmount,
            List<Stock> eligibleStocks,
            AllocationStrategy config) {

        if (eligibleStocks == null || eligibleStocks.isEmpty()) {
            return ProposedAllocation.builder()
                    .strategy(config)
                    .allocations(Collections.emptyMap())
                    .totalProposed(BigDecimal.ZERO)
                    .unallocated(investableAmount)
                    .validationMessages(List.of("No eligible stocks available"))
                    .valid(false)
                    .build();
        }

        // Reserve cash buffer
        BigDecimal cashBuffer = investableAmount
                .multiply(config.getCashBufferPercent())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal deployable = investableAmount.subtract(cashBuffer);

        // Max per stock
        BigDecimal maxPerStock = deployable
                .multiply(config.getMaxStockConcentration())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        // Apply profile-specific weights
        Map<Stock, BigDecimal> weights = computeWeights(eligibleStocks, config);
        BigDecimal totalWeight = weights.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Stock, BigDecimal> allocations = new LinkedHashMap<>();
        BigDecimal totalAllocated = BigDecimal.ZERO;

        for (Map.Entry<Stock, BigDecimal> entry : weights.entrySet()) {
            BigDecimal proportional = deployable
                    .multiply(entry.getValue())
                    .divide(totalWeight, 4, RoundingMode.HALF_UP);
            BigDecimal amount = proportional.min(maxPerStock);

            // Enforce sector cap at strategy level (per-stock contribution)
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                allocations.put(entry.getKey(), amount);
                totalAllocated = totalAllocated.add(amount);
            }
        }

        BigDecimal unallocated = investableAmount.subtract(totalAllocated);

        return ProposedAllocation.builder()
                .strategy(config)
                .allocations(allocations)
                .totalProposed(totalAllocated)
                .unallocated(unallocated)
                .validationMessages(Collections.emptyList())
                .valid(true)
                .build();
    }
    
     //Subclasses override to assign relative weights to stocks.
     // Default: equal weight for all stocks.

    protected Map<Stock, BigDecimal> computeWeights(List<Stock> stocks, AllocationStrategy config) {
        Map<Stock, BigDecimal> weights = new LinkedHashMap<>();
        for (Stock s : stocks) {
            weights.put(s, BigDecimal.ONE);
        }
        return weights;
    }
}

