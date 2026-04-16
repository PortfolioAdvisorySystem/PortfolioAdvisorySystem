package com.backend.stockAllocation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyAllocationRequest {

    @NotNull(message = "Strategy ID is required")
    private Long strategyId;

    @NotNull(message = "Allocation percent is required")
    @DecimalMin(value = "0.01", message = "Allocation percent must be > 0")
    @DecimalMax(value = "100.00", message = "Allocation percent must be <= 100")
    private BigDecimal allocationPercent;
}
