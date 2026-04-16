package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.enums.RiskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findByIsActiveTrue();
    List<Stock> findByIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse();
    List<Stock> findBySectorAndIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse(String sector);
    List<Stock> findByRiskCategoryAndIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse(RiskCategory riskCategory);

    @Query("SELECT s FROM Stock s WHERE s.isActive = true AND s.isSuspended = false " +
            "AND s.isBlacklisted = false AND s.avgVolume >= :minVolume")
    List<Stock> findEligibleStocksByMinVolume(@Param("minVolume") BigDecimal minVolume);

    @Query("SELECT COUNT(DISTINCT p.portfolio.subscriber.id) FROM Position p WHERE p.stock.id = :stockId")
    long countSubscribersHoldingStock(@Param("stockId") Long stockId);
}
