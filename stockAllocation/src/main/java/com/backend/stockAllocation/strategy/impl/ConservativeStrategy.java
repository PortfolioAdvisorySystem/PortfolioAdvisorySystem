package com.backend.stockAllocation.strategy.impl;
import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.enums.RiskCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


 // Conservative: over-weights LOW-risk stocks; skips HIGH-risk.

@Component("CONSERVATIVE")
public class ConservativeStrategy extends BaseAllocationStrategy {

    @Override
    protected Map<Stock, BigDecimal> computeWeights(List<Stock> stocks, AllocationStrategy config) {
        Map<Stock, BigDecimal> weights = new LinkedHashMap<>();
        for (Stock s : stocks) {
            BigDecimal weight = switch (s.getRiskCategory()) {
                case LOW    -> new BigDecimal("3.0");
                case MEDIUM -> new BigDecimal("1.0");
                case HIGH   -> BigDecimal.ZERO; // Conservative avoids HIGH
            };
            if (weight.compareTo(BigDecimal.ZERO) > 0) {
                weights.put(s, weight);
            }
        }
        return weights;
    }
}
