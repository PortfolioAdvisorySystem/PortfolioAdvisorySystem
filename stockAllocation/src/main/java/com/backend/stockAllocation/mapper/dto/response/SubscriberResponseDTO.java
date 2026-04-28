package com.backend.stockAllocation.mapper.dto.response;

import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.enums.SubscriberStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SubscriberResponseDTO {
    private Long id;
    private String name;
    private String email;
    private BigDecimal investmentAmount;
    private RiskProfile riskProfile;
    private String plan;
    private SubscriberStatus status;
}
