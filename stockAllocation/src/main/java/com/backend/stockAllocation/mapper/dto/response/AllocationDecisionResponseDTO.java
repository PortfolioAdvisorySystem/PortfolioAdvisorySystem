package com.backend.stockAllocation.mapper.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class AllocationDecisionResponseDTO {
    private Long id;
    private String runId;
    private String stockSymbol;
    private String strategyName;
    private BigDecimal amountAllocated;
    private BigDecimal weightPercent;
    private String runType;
    private String ruleBasis;
    private LocalDateTime createdAt;
}
