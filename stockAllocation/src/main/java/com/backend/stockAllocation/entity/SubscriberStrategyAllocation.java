package com.backend.stockAllocation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscriber_strategy_allocations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subscriber_id", "strategy_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberStrategyAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id")
    @JsonBackReference
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_id")
    @JsonBackReference
    private AllocationStrategy strategy;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal allocationPercent;

    @Column(nullable = false)
    private BigDecimal allocatedAmount;
}

