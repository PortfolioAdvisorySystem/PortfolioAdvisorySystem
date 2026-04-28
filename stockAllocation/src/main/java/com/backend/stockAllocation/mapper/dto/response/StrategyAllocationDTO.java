package com.backend.stockAllocation.mapper.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StrategyAllocationDTO {
    private String strategyName;
    private BigDecimal totalAllocation;
    private BigDecimal percentage;
    private Long positionCount;
}
