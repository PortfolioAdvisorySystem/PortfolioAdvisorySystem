package com.backend.stockAllocation.strategy;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Stock;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProposedAllocation {
    private final AllocationStrategy strategy;
    // stock → proposed amount
    private final Map<Stock, BigDecimal> allocations;
    private final BigDecimal totalProposed;
    private final BigDecimal unallocated;
    private final List<String> validationMessages;
    private final boolean valid;
}
