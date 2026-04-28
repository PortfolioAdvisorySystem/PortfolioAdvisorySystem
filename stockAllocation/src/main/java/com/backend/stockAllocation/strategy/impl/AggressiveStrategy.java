package com.backend.stockAllocation.strategy.impl;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Stock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


//  Aggressive: over-weights HIGH-risk, growth-oriented stocks.

@Component("AGGRESSIVE")
public class AggressiveStrategy extends BaseAllocationStrategy {

    @Override
    protected Map<Stock, BigDecimal> computeWeights(List<Stock> stocks, AllocationStrategy config) {
        Map<Stock, BigDecimal> weights = new LinkedHashMap<>();
        for (Stock s : stocks) {
            BigDecimal weight = switch (s.getRiskCategory()) {
                case LOW    -> new BigDecimal("0.5");
                case MEDIUM -> new BigDecimal("1.5");
                case HIGH   -> new BigDecimal("3.0");
            };
            weights.put(s, weight);
        }
        return weights;
    }
}

