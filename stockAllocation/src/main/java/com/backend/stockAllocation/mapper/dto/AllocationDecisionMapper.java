package com.backend.stockAllocation.mapper.dto;

import com.backend.stockAllocation.entity.AllocationDecision;
import com.backend.stockAllocation.mapper.dto.response.AllocationDecisionResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllocationDecisionMapper {
    public AllocationDecisionResponseDTO toResponseDTO(AllocationDecision decision) {
        return AllocationDecisionResponseDTO.builder()
                .id(decision.getId())
                .runId(decision.getRunId())
                .stockSymbol(decision.getStock().getSymbol())
                .strategyName(decision.getStrategy() != null
                        ? decision.getStrategy().getName() : "N/A")
                .amountAllocated(decision.getAmountAllocated())
                .weightPercent(decision.getWeightPercent())
                .runType(decision.getRunType() != null
                        ? decision.getRunType().name() : "N/A")
                .ruleBasis(decision.getRuleBasis())
                .createdAt(decision.getCreatedAt())
                .build();
    }

    // ── List mapper ────────────────────────────────────────────────────────
    public List<AllocationDecisionResponseDTO> toResponseDTOList(
            List<AllocationDecision> decisions) {
        return decisions.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
