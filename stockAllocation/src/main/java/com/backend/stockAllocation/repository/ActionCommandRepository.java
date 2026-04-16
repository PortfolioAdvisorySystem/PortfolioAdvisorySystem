package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.ActionCommand;
import com.backend.stockAllocation.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionCommandRepository extends JpaRepository<ActionCommand, Long> {
    List<ActionCommand> findByApprovalStatus(ApprovalStatus status);
    List<ActionCommand> findBySubmittedBy(String submittedBy);
}
