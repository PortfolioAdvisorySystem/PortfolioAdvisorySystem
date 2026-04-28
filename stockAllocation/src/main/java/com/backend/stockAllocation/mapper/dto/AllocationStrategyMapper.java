package com.backend.stockAllocation.mapper.dto;

import com.backend.stockAllocation.dto.request.AllocationStrategyRequest;
import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.mapper.dto.response.AllocationStrategyResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllocationStrategyMapper {
    public AllocationStrategy toEntity(AllocationStrategyRequest dto) {
        AllocationStrategy strategy = new AllocationStrategy();
        strategy.setName(dto.getName());
        strategy.setRiskProfile(dto.getRiskProfile());
        strategy.setMaxStockConcentration(dto.getMaxStockConcentration());
        strategy.setMaxSectorExposure(dto.getMaxSectorExposure());
        strategy.setCashBufferPercent(dto.getCashBufferPercent());
        strategy.setActive(true);
        return strategy;
    }

    public AllocationStrategyResponseDTO toResponseDTO(AllocationStrategy strategy) {
        return AllocationStrategyResponseDTO.builder()
                .id(strategy.getId())
                .name(strategy.getName())
                .riskProfile(strategy.getRiskProfile())
                .maxStockConcentration(strategy.getMaxStockConcentration())
                .maxSectorExposure(strategy.getMaxSectorExposure())
                .cashBufferPercent(strategy.getCashBufferPercent())
                .active(strategy.isActive())
                .build();
    }


    public List<AllocationStrategyResponseDTO> toResponseDTOList(
            List<AllocationStrategy> strategies) {
        return strategies.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(AllocationStrategyRequest dto,
                                    AllocationStrategy strategy) {
        strategy.setName(dto.getName());
        strategy.setRiskProfile(dto.getRiskProfile());
        strategy.setMaxStockConcentration(dto.getMaxStockConcentration());
        strategy.setMaxSectorExposure(dto.getMaxSectorExposure());
        strategy.setCashBufferPercent(dto.getCashBufferPercent());
    }
}
