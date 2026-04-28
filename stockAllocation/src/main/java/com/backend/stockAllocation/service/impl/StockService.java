package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.dto.request.StockRequest;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
import com.backend.stockAllocation.mapper.dto.StockMapper;
import com.backend.stockAllocation.mapper.dto.response.StockResponseDto;
import com.backend.stockAllocation.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import com.backend.stockAllocation.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final AuditService auditService;
    private final RebalanceManager rebalanceManager;
    private final StockMapper stockMapper;
    @Transactional
    public StockResponseDto create(StockRequest request) {
        if (stockRepository.findBySymbol(request.getSymbol()).isPresent()) {
            throw new BadRequestException("Stock already exists: " + request.getSymbol());
        }
        Stock stock = Stock.builder()
                .symbol(request.getSymbol().toUpperCase())
                .sector(request.getSector())
                .category(request.getCategory())
                .liquidityScore(request.getLiquidityScore())
                .avgVolume(request.getAvgVolume())
                .marketCap(request.getMarketCap())
                .riskCategory(request.getRiskCategory())
                .isActive(true)
                .isSuspended(false)
                .isBlacklisted(false)
                .build();
        stockRepository.save(stock);
        auditService.log("STOCK_CREATED", "Stock " + stock.getSymbol() + " created",
                "SYSTEM", stock.getId(), "Stock");

        return stockMapper.toResponseDTO(stock);
    }

    @Transactional
    public StockResponseDto update(Long id, StockRequest request) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.setSector(request.getSector());
        stock.setCategory(request.getCategory());
        stock.setLiquidityScore(request.getLiquidityScore());
        stock.setAvgVolume(request.getAvgVolume());
        stock.setMarketCap(request.getMarketCap());
        stock.setRiskCategory(request.getRiskCategory());
         stockRepository.save(stock);
         return stockMapper.toResponseDTO(stock);
    }

    @Transactional
    public StockResponseDto deactivate(Long id, String reason) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.setActive(false);
        StockResponseDto s=stockMapper.toResponseDTO(stock);
        stockRepository.save(stock);
        auditService.log("STOCK_DEACTIVATED", "Stock " + stock.getSymbol() + " deactivated: " + reason,
                "SYSTEM", id, "Stock");
        // Trigger event-driven rebalance
        rebalanceManager.triggerEventRebalance("Stock deactivated: " + stock.getSymbol(), "SYSTEM");
        return s;
    }

    @Transactional
    public StockResponseDto suspend(Long id) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.setSuspended(true);
        stock.setActive(false);
        stockRepository.save(stock);
        rebalanceManager.triggerEventRebalance("Stock suspended: " + stock.getSymbol(), "SYSTEM");
        return stockMapper.toResponseDTO(stock);
    }

    @Transactional
    public StockResponseDto unsuspend(Long id) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.setSuspended(false);
        stock.setActive(true);
        stockRepository.save(stock);
        return stockMapper.toResponseDTO(stock);
    }

    @Transactional
    public StockResponseDto blacklist(Long id) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.setActive(false);
        stock.setSuspended(true);
        stock.setBlacklisted(true);
        stockRepository.save(stock);
        rebalanceManager.triggerEventRebalance("Stock blacklisted: " + stock.getSymbol(), "SYSTEM");
        return stockMapper.toResponseDTO(stock);
    }

    public StockResponseDto getById(Long id) {
        Stock s=stockRepository.findById(id).orElseThrow();
        return stockMapper.toResponseDTO(s);

    }

    public List<StockResponseDto> getAll() {
        List<Stock>stocks=stockRepository.findAll();
        List<StockResponseDto>ans=stockMapper.toResponseDTOList(stocks);

        return ans;
    }
    public List<StockResponseDto> getEligible() {
        List<Stock>stocks=stockRepository.findByIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse();
        return stockMapper.toResponseDTOList(stocks);
    }
}
