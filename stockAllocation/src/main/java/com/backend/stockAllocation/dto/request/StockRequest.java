package com.backend.stockAllocation.dto.request;

import com.backend.stockAllocation.enums.RiskCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Sector is required")
    private String sector;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 0, message = "Liquidity score must be >= 0")
    @Max(value = 100, message = "Liquidity score must be <= 100")
    private Integer liquidityScore;

    @NotNull(message = "Average volume is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal avgVolume;

    @NotNull(message = "Market cap is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal marketCap;

    @NotNull(message = "Risk category is required")
    private RiskCategory riskCategory;
}

