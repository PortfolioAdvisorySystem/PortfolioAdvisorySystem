package com.backend.stockAllocation.mapper.dto.response;

import com.backend.stockAllocation.enums.RiskCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StockResponseDto {
    private Long id;
    private String symbol;
    private String sector;
    private String category;
    private Integer liquidityScore;
    private BigDecimal avgVolume;
    private BigDecimal marketCap;
    private RiskCategory riskCategory;
    private boolean active;
    private boolean blacklisted;
    private boolean suspended;
}
