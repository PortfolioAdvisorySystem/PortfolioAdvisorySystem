package com.backend.stockAllocation.dto.request;

import com.backend.stockAllocation.enums.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    private BigDecimal threshold;
    private String targetStockSymbol;
    private String targetSector;
    private String targetRiskProfile;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private int priority;
    private String description;
}
