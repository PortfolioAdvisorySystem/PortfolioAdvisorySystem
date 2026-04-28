package com.backend.stockAllocation.mapper.dto.response;

import com.backend.stockAllocation.enums.RiskProfile;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class AllocationStrategyResponseDTO {
    private Long id;
    private String name;
    private RiskProfile riskProfile;
    private BigDecimal maxStockConcentration;
    private BigDecimal maxSectorExposure;
    private BigDecimal cashBufferPercent;
    private boolean active;
}
