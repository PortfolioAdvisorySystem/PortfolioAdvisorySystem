package com.backend.stockAllocation.mapper.dto.response;

import com.backend.stockAllocation.enums.RiskCategory;
import com.backend.stockAllocation.enums.RiskProfile;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class PositionResponseDto {
    private Long id;
    private String stockSymbol;
    private String sector;
    private RiskCategory riskCategory;
    private String strategyName;
    private RiskProfile riskProfile;
    private BigDecimal weight;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private LocalDateTime allocatedAt;
    private boolean markedForDeallocation;
    private String deallocationReason;
    private Long subscriberId;
}
