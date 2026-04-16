package com.backend.stockAllocation.entity;

import com.backend.stockAllocation.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;
    private BigDecimal threshold;

    private String targetStockSymbol;

    private String targetSector;

    private String targetRiskProfile;

    @Column(nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private int priority = 0;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String ruleSnapshot;
}

