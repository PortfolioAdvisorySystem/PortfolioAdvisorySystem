package com.backend.stockAllocation.repository;
import com.backend.stockAllocation.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByPortfolioId(Long portfolioId);
    List<Position> findByStockId(Long stockId);
    List<Position> findByMarkedForDeallocationTrue();
    Optional<Position> findByPortfolioIdAndStockIdAndStrategyId(Long portfolioId, Long stockId, Long strategyId);

    @Query("SELECT p FROM Position p WHERE p.stock.id = :stockId AND p.markedForDeallocation = false")
    List<Position> findActivePositionsByStockId(@Param("stockId") Long stockId);

    @Query("SELECT COALESCE(SUM(p.weight), 0) FROM Position p " +
            "WHERE p.portfolio.id = :portfolioId AND p.stock.sector = :sector AND p.markedForDeallocation = false")
    BigDecimal sumWeightBySector(@Param("portfolioId") Long portfolioId, @Param("sector") String sector);

    @Query("SELECT COALESCE(SUM(p.weight), 0) FROM Position p " +
            "WHERE p.portfolio.id = :portfolioId AND p.stock.id = :stockId AND p.markedForDeallocation = false")
    BigDecimal sumWeightByStock(@Param("portfolioId") Long portfolioId, @Param("stockId") Long stockId);

    @Query("SELECT COALESCE(SUM(p.weight), 0) FROM Position p " +
            "WHERE p.portfolio.id = :portfolioId AND p.markedForDeallocation = false")
    BigDecimal sumTotalAllocated(@Param("portfolioId") Long portfolioId);
}

