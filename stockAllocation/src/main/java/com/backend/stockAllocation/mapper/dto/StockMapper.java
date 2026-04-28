package com.backend.stockAllocation.mapper.dto;

import com.backend.stockAllocation.dto.request.StockRequest;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.mapper.dto.response.StockResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockMapper {
    public Stock toEntity(StockRequest dto) {
        Stock stock = new Stock();
        stock.setSymbol(dto.getSymbol());
        stock.setSector(dto.getSector());
        stock.setCategory(dto.getCategory());
        stock.setLiquidityScore(dto.getLiquidityScore());
        stock.setAvgVolume(dto.getAvgVolume());
        stock.setMarketCap(dto.getMarketCap());
        stock.setRiskCategory(dto.getRiskCategory());
        stock.setActive(true);           // default on creation
        stock.setBlacklisted(false);     // default on creation
        stock.setSuspended(false);       // default on creation
        return stock;
    }

    public StockResponseDto toResponseDTO(Stock stock) {
        return StockResponseDto.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .sector(stock.getSector())
                .category(stock.getCategory())
                .liquidityScore(stock.getLiquidityScore())
                .avgVolume(stock.getAvgVolume())
                .marketCap(stock.getMarketCap())
                .riskCategory(stock.getRiskCategory())
                .active(stock.isActive())
                .blacklisted(stock.isBlacklisted())
                .suspended(stock.isSuspended())
                .build();
    }

    public List<StockResponseDto> toResponseDTOList(List<Stock> stocks) {
        return stocks.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(StockRequest dto, Stock stock) {
        stock.setSymbol(dto.getSymbol());
        stock.setSector(dto.getSector());
        stock.setCategory(dto.getCategory());
        stock.setLiquidityScore(dto.getLiquidityScore());
        stock.setAvgVolume(dto.getAvgVolume());
        stock.setMarketCap(dto.getMarketCap());
        stock.setRiskCategory(dto.getRiskCategory());
    }
}
