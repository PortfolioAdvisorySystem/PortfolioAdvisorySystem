package com.backend.stockAllocation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "migration_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_stock_id")
    @JsonBackReference
    private Stock sourceStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_stock_id")
    @JsonBackReference
    private Stock targetStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    @JsonBackReference
    private Subscriber subscriber;

    @Column(nullable = false)
    private BigDecimal amountShifted;

    @Column(nullable = false)
    private String migrationReason;

    private String ruleTrigger;

    private String approvalReference;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime migratedAt = LocalDateTime.now();
}

