package com.backend.stockAllocation.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class InflowRequest {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
