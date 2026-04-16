package com.backend.stockAllocation.entity;

import com.backend.stockAllocation.enums.RiskProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "allocation_strategies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskProfile riskProfile;

    @Column(nullable = false)
    private BigDecimal maxStockConcentration;

    @Column(nullable = false)
    private BigDecimal maxSectorExposure;

    @Column(nullable = false)
    private BigDecimal cashBufferPercent;

    private boolean isActive = true;
}

