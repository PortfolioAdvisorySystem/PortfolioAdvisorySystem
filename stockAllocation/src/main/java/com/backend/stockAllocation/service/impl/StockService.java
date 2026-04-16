package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.dto.request.StockRequest;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
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

    @Transactional
    public Stock create(StockRequest request) {
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
        return stock;
    }

    @Transactional
    public Stock update(Long id, StockRequest request) {
        Stock stock = getById(id);
        stock.setSector(request.getSector());
        stock.setCategory(request.getCategory());
        stock.setLiquidityScore(request.getLiquidityScore());
        stock.setAvgVolume(request.getAvgVolume());
        stock.setMarketCap(request.getMarketCap());
        stock.setRiskCategory(request.getRiskCategory());
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock deactivate(Long id, String reason) {
        Stock stock = getById(id);
        stock.setActive(false);
        stockRepository.save(stock);
        auditService.log("STOCK_DEACTIVATED", "Stock " + stock.getSymbol() + " deactivated: " + reason,
                "SYSTEM", id, "Stock");
        // Trigger event-driven rebalance
        rebalanceManager.triggerEventRebalance("Stock deactivated: " + stock.getSymbol(), "SYSTEM");
        return stock;
    }

    @Transactional
    public Stock suspend(Long id) {
        Stock stock = getById(id);
        stock.setSuspended(true);
        stockRepository.save(stock);
        rebalanceManager.triggerEventRebalance("Stock suspended: " + stock.getSymbol(), "SYSTEM");
        return stock;
    }

    @Transactional
    public Stock unsuspend(Long id) {
        Stock stock = getById(id);
        stock.setSuspended(false);
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock blacklist(Long id) {
        Stock stock = getById(id);
        stock.setBlacklisted(true);
        stockRepository.save(stock);
        rebalanceManager.triggerEventRebalance("Stock blacklisted: " + stock.getSymbol(), "SYSTEM");
        return stock;
    }

    public Stock getById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));
    }

    public List<Stock> getAll() { return stockRepository.findAll(); }
    public List<Stock> getEligible() {
        return stockRepository.findByIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse();
    }
}
