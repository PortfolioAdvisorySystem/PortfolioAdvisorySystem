package com.backend.stockAllocation.entity;

import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.enums.SubscriberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private BigDecimal investmentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskProfile primaryRiskProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriberStatus status = SubscriberStatus.ACTIVE;

    @OneToOne(mappedBy = "subscriber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Portfolio portfolio;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubscriberStrategyAllocation> strategyAllocations = new ArrayList<>();

    public BigDecimal getAUM() {
        return investmentAmount;
    }
}

