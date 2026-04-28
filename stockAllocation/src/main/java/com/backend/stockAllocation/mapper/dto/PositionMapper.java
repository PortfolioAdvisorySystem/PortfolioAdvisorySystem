package com.backend.stockAllocation.mapper.dto;

import com.backend.stockAllocation.entity.Position;
import com.backend.stockAllocation.mapper.dto.response.PositionResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PositionMapper {

    public PositionResponseDto toResponseDTO(Position position) {
        return PositionResponseDto.builder()
                .id(position.getId())
                .stockSymbol(position.getStock().getSymbol())
                .sector(position.getStock().getSector())
                .riskCategory(position.getStock().getRiskCategory())
                .strategyName(position.getStrategy() != null
                        ? position.getStrategy().getName() : "N/A")
                .riskProfile(position.getStrategy().getRiskProfile())
                .weight(position.getWeight())
                .quantity(position.getQuantity())
                .purchasePrice(position.getPurchasePrice())
                .allocatedAt(position.getAllocatedAt())
                .markedForDeallocation(position.isMarkedForDeallocation())
                .deallocationReason(position.getDeallocationReason())
                .subscriberId(position.getPortfolio().getSubscriber().getId())
                .build();
    }

    public List<PositionResponseDto> toResponseDTOList(List<Position> positions) {
        return positions.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
