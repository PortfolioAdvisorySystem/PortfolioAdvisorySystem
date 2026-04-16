package com.backend.stockAllocation.entity;

import com.backend.stockAllocation.enums.ActionType;
import com.backend.stockAllocation.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String state;

    @Column(nullable = false)
    private String submittedBy;

    private String reviewedBy;

    private String reviewNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    private boolean emergencyOverride = false;
}
