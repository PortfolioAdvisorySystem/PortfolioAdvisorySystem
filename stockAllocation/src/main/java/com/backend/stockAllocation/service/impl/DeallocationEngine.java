package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.repository.PortfolioRepository;
import com.backend.stockAllocation.repository.PositionRepository;
import com.backend.stockAllocation.repository.RuleRepository;
import com.backend.stockAllocation.repository.StockRepository;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluator;
import com.backend.stockAllocation.rule.engine.StockEligibilityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class DeallocationEngine {
    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final RuleRepository ruleRepository;
    private final RuleEvaluator ruleEvaluator;
    private final AuditService auditService;


     //Scan all positions and mark those that violate active rules for deallocation.
     // Returns the list of positions marked.


    @Transactional
    public List<Position> detectAndMarkForDeallocation() {
        List<Position> allActive = positionRepository.findAll().stream()
                .filter(p -> !p.isMarkedForDeallocation())
                .toList();

        List<Rule> activeRules = ruleRepository.findCurrentlyActiveRules(LocalDate.now());
        List<Position> marked = new ArrayList<>();

        for (Position position : allActive) {
            Portfolio portfolio = position.getPortfolio();
            Subscriber subscriber = portfolio.getSubscriber();
            Stock stock = position.getStock();

            long subCount = stockRepository.countSubscribersHoldingStock(stock.getId());
            BigDecimal currentSectorWeight = positionRepository.sumWeightBySector(
                    portfolio.getId(), stock.getSector());
            BigDecimal totalAllocated = positionRepository.sumTotalAllocated(portfolio.getId());

            RuleEvaluationContext ctx = RuleEvaluationContext.builder()
                    .subscriber(subscriber)
                    .stock(stock)
                    .strategy(position.getStrategy())
                    .portfolio(portfolio)
                    .currentStockWeight(position.getWeight())
                    .currentSectorWeight(currentSectorWeight != null ? currentSectorWeight : BigDecimal.ZERO)
                    .totalAllocated(totalAllocated != null ? totalAllocated : BigDecimal.ZERO)
                    .totalAUM(subscriber.getInvestmentAmount())
                    .subscriberCountForStock(subCount)
                    .stockMetrics(Collections.emptyMap())
                    .proposedAmount(BigDecimal.ZERO)
                    .build();

            StockEligibilityResult result = ruleEvaluator.checkWithRules(activeRules, ctx);

            if (!result.isEligible()) {
                String reason = result.getSummary();
                markForDeallocation(position, reason);
                marked.add(position);
                log.info("Position {} marked for deallocation: {}", position.getId(), reason);
            }
        }

        auditService.log("DEALLOCATION_SCAN",
                "Marked " + marked.size() + " positions for deallocation", "SYSTEM", null, "System");
        return marked;
    }


     // Execute full deallocation of a specific position.

    @Transactional
    public void executeDeallocation(List<Position> positions) {
        for (Position position : positions) {
            Portfolio portfolio = position.getPortfolio();
            BigDecimal freed = position.getWeight();

            // Move freed amount to unallocated pool
            portfolio.setUnallocatedAmount(portfolio.getUnallocatedAmount().add(freed));
            portfolioRepository.save(portfolio);

            // Remove position
            positionRepository.delete(position);

            auditService.log("POSITION_DEALLOCATED",
                    "Position id=" + position.getId() + " stock=" + position.getStock().getSymbol() +
                            " amount=" + freed + " reason=" + position.getDeallocationReason(),
                    "SYSTEM", position.getId(), "Position");
        }
    }

    @Transactional
    public void recordDeallocationReason(List<Position> positions, String reason) {
        for (Position pos : positions) {
            pos.setDeallocationReason(reason);
            positionRepository.save(pos);
        }
    }

    private void markForDeallocation(Position position, String reason) {
        position.setMarkedForDeallocation(true);
        position.setDeallocationReason(reason);
        positionRepository.save(position);
    }


     // Retrieve all positions currently marked for deallocation.

    public List<Position> getPositionsMarkedForDeallocation() {
        return positionRepository.findByMarkedForDeallocationTrue();
    }
}
