package com.backend.stockAllocation.dto.request;

import com.backend.stockAllocation.enums.RiskProfile;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationStrategyRequest {

    @NotBlank
    private String name;

    @NotNull
    private RiskProfile riskProfile;

    @NotNull @DecimalMin("0.01") @DecimalMax("100.00")
    private BigDecimal maxStockConcentration;

    @NotNull @DecimalMin("0.01") @DecimalMax("100.00")
    private BigDecimal maxSectorExposure;

    @NotNull @DecimalMin("0.00") @DecimalMax("100.00")
    private BigDecimal cashBufferPercent;
}