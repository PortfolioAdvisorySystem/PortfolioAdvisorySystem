package com.backend.stockAllocation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rebalance_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RebalanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String triggerReason;

    private String triggeredBy;

    @Column(nullable = false)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime triggeredAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}

