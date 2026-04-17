package com.backend.stockAllocation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", unique = true)
    @JsonBackReference
    private Subscriber subscriber;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<Position> positions = new ArrayList<>();

    /** Amount not yet allocated to any stock */
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal unallocatedAmount = BigDecimal.ZERO;

    public void addPosition(Position position) {
        positions.add(position);
        position.setPortfolio(this);
    }

    public void removePosition(Position position) {
        positions.remove(position);
        position.setPortfolio(null);
    }

    public BigDecimal calculateHeadroomForStock(Stock stock) {
        return positions.stream()
                .filter(p -> p.getStock().equals(stock))
                .map(Position::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

