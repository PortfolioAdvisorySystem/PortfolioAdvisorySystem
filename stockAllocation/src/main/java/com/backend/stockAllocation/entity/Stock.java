package com.backend.stockAllocation.entity;

import com.backend.stockAllocation.enums.RiskCategory;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String category;

    private Integer liquidityScore;

    @Column(nullable = false)
    private BigDecimal avgVolume;

    @Column(nullable = false)
    private BigDecimal marketCap;

    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory;

    @Column(nullable = false)
    private boolean isActive = true;

    private boolean isSuspended = false;

    private boolean isBlacklisted = false;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<Position> positions = new ArrayList<>();
}

