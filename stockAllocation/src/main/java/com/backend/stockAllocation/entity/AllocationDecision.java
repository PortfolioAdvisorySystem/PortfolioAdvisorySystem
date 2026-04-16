package com.backend.stockAllocation.entity;

import com.backend.stockAllocation.enums.AllocationRunType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.Flow;

@Entity
@Table(name = "allocation_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String runId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    private AllocationStrategy strategy;

    @Column(nullable = false)
    private BigDecimal amountAllocated;

    @Column(nullable = false)
    private BigDecimal weightPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationRunType runType;

    private String ruleBasis;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

