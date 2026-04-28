package com.backend.stockAllocation.strategy.impl;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Stock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


 // Moderate: balanced weighting across LOW and MEDIUM; limited HIGH exposure.

@Component("MODERATE")
public class ModerateStrategy extends BaseAllocationStrategy {

    @Override
    protected Map<Stock, BigDecimal> computeWeights(List<Stock> stocks, AllocationStrategy config) {
        Map<Stock, BigDecimal> weights = new LinkedHashMap<>();
        for (Stock s : stocks) {
            BigDecimal weight = switch (s.getRiskCategory()) {
                case LOW    -> new BigDecimal("2.0");
                case MEDIUM -> new BigDecimal("2.5");
                case HIGH   -> new BigDecimal("0.5");
            };
            weights.put(s, weight);
        }
        return weights;
    }
}
