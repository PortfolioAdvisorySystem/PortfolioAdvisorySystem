package com.backend.stockAllocation.dto.request;

import com.backend.stockAllocation.enums.RiskProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "0.01", message = "Investment amount must be positive")
    private BigDecimal investmentAmount;

    @NotNull(message = "Primary risk profile is required")
    private RiskProfile primaryRiskProfile;

    @NotEmpty(message = "At least one strategy allocation is required")
    @Valid
    private List<StrategyAllocationRequest> strategyAllocations;
}
