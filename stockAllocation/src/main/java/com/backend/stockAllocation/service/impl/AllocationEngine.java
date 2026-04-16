package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.enums.AllocationRunType;
import com.backend.stockAllocation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationEngine {

    private final StockRepository stockRepository;
    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;
    private final AllocationDecisionRepository allocationDecisionRepository;
    private final SubscriberStrategyAllocationRepository ssaRepository;
    private final RuleEvaluator ruleEvaluator;
    private final StrategyResolver strategyResolver;
    private final AuditService auditService;

    /**
     * Allocate for a single subscriber across all their strategy slices.
     */
    @Transactional
    public List<AllocationDecision> allocate(Subscriber subscriber,
                                             BigDecimal inflows,
                                             AllocationRunType runType) {
        log.info("Starting {} allocation for subscriber {}", runType, subscriber.getId());
        String runId = UUID.randomUUID().toString();

        List<AllocationDecision> decisions = new ArrayList<>();

        Portfolio portfolio = portfolioRepository.findBySubscriberId(subscriber.getId())
                .orElseGet(() -> createPortfolio(subscriber));

        List<SubscriberStrategyAllocation> strategySlices = ssaRepository.findBySubscriberId(subscriber.getId());

        if (strategySlices.isEmpty()) {
            log.warn("No strategy allocations found for subscriber {}", subscriber.getId());
            return decisions;
        }

        for (SubscriberStrategyAllocation slice : strategySlices) {
            BigDecimal sliceAmount = slice.getAllocatedAmount();
            if (inflows != null && inflows.compareTo(BigDecimal.ZERO) > 0) {
                // Distribute inflows proportionally by slice percentage
                BigDecimal inflowShare = inflows
                        .multiply(slice.getAllocationPercent())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                sliceAmount = inflowShare;
            }

            List<AllocationDecision> sliceDecisions = allocateForSlice(
                    subscriber, portfolio, slice.getStrategy(), sliceAmount, runId, runType);
            decisions.addAll(sliceDecisions);
        }

        auditService.log("ALLOCATION_RUN", "Run " + runId + " for subscriber " + subscriber.getId() +
                " type=" + runType + " decisions=" + decisions.size(), "SYSTEM", subscriber.getId(), "Subscriber");
        return decisions;
    }



    private List<AllocationDecision> allocateForSlice(Subscriber subscriber,
                                                      Portfolio portfolio,
                                                      AllocationStrategy strategyConfig,
                                                      BigDecimal investableAmount,
                                                      String runId,
                                                      AllocationRunType runType) {

        List<Stock> allStocks = stockRepository.findByIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse();

        BigDecimal totalAllocated = positionRepository.sumTotalAllocated(portfolio.getId());
        if (totalAllocated == null) totalAllocated = BigDecimal.ZERO;

        // Build context template for rule evaluation
        RuleEvaluationContext templateCtx = RuleEvaluationContext.builder()
                .subscriber(subscriber)
                .strategy(strategyConfig)
                .portfolio(portfolio)
                .totalAUM(subscriber.getInvestmentAmount())
                .totalAllocated(totalAllocated)
                .proposedAmount(BigDecimal.ZERO)
                .currentStockWeight(BigDecimal.ZERO)
                .currentSectorWeight(BigDecimal.ZERO)
                .subscriberCountForStock(0)
                .stockMetrics(Collections.emptyMap())
                .build();

        List<Stock> eligibleStocks = ruleEvaluator.filterEligibleStocks(allStocks, templateCtx);
        log.debug("Eligible stocks for strategy {}: {}", strategyConfig.getName(), eligibleStocks.size());

        AllocationStrategyInterface strategyImpl = strategyResolver.resolve(strategyConfig.getRiskProfile());
        ProposedAllocation proposed = strategyImpl.generateInitialAllocation(
                subscriber, investableAmount, eligibleStocks, strategyConfig);

        if (!proposed.isValid()) {
            log.warn("Proposed allocation invalid for strategy {}: {}", strategyConfig.getName(),
                    proposed.getValidationMessages());
            return Collections.emptyList();
        }

        List<AllocationDecision> decisions = new ArrayList<>();

        for (Map.Entry<Stock, BigDecimal> entry : proposed.getAllocations().entrySet()) {
            Stock stock = entry.getKey();
            BigDecimal amount = entry.getValue();

            if (amount.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Validate final allocation against rules with proposed amount
            BigDecimal currentStockWeight = positionRepository.sumWeightByStock(portfolio.getId(), stock.getId());
            BigDecimal currentSectorWeight = positionRepository.sumWeightBySector(portfolio.getId(), stock.getSector());
            long subCount = stockRepository.countSubscribersHoldingStock(stock.getId());

            RuleEvaluationContext finalCtx = RuleEvaluationContext.builder()
                    .subscriber(subscriber)
                    .stock(stock)
                    .strategy(strategyConfig)
                    .portfolio(portfolio)
                    .currentStockWeight(currentStockWeight != null ? currentStockWeight : BigDecimal.ZERO)
                    .currentSectorWeight(currentSectorWeight != null ? currentSectorWeight : BigDecimal.ZERO)
                    .totalAllocated(totalAllocated)
                    .totalAUM(subscriber.getInvestmentAmount())
                    .subscriberCountForStock(subCount)
                    .stockMetrics(Collections.emptyMap())
                    .proposedAmount(amount)
                    .build();

            StockEligibilityResult validation = ruleEvaluator.checkStockEligibility(finalCtx);
            if (!validation.isEligible()) {
                log.warn("Final validation failed for stock {}: {}", stock.getSymbol(), validation.getSummary());
                continue;
            }

            // Create or update position
            upsertPosition(portfolio, stock, strategyConfig, amount);

            // Record decision
            BigDecimal weightPct = amount.divide(subscriber.getInvestmentAmount(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            AllocationDecision decision = AllocationDecision.builder()
                    .runId(runId)
                    .subscriber(subscriber)
                    .stock(stock)
                    .strategy(strategyConfig)
                    .amountAllocated(amount)
                    .weightPercent(weightPct)
                    .runType(runType)
                    .ruleBasis(validation.getSummary())
                    .build();
            allocationDecisionRepository.save(decision);
            decisions.add(decision);
        }

        // Update unallocated balance
        portfolio.setUnallocatedAmount(proposed.getUnallocated());
        portfolioRepository.save(portfolio);

        return decisions;
    }

    private void upsertPosition(Portfolio portfolio, Stock stock, AllocationStrategy strategy, BigDecimal amount) {
        Optional<Position> existing = positionRepository
                .findByPortfolioIdAndStockIdAndStrategyId(portfolio.getId(), stock.getId(), strategy.getId());

        if (existing.isPresent()) {
            Position pos = existing.get();
            pos.setWeight(pos.getWeight().add(amount));
            positionRepository.save(pos);
        } else {
            Position pos = Position.builder()
                    .portfolio(portfolio)
                    .stock(stock)
                    .strategy(strategy)
                    .weight(amount)
                    .quantity(BigDecimal.ONE) // placeholder; real quantity = amount / price
                    .purchasePrice(BigDecimal.ONE)
                    .build();
            positionRepository.save(pos);
            portfolio.getPositions().add(pos);
        }
    }

    private Portfolio createPortfolio(Subscriber subscriber) {
        Portfolio portfolio = Portfolio.builder()
                .subscriber(subscriber)
                .unallocatedAmount(BigDecimal.ZERO)
                .positions(new ArrayList<>())
                .build();
        return portfolioRepository.save(portfolio);
    }

    /**
     * Apply inflows to subscriber portfolio.
     */
    @Transactional
    public List<AllocationDecision> applyInflows(Subscriber subscriber, BigDecimal inflows) {
        return allocate(subscriber, inflows, AllocationRunType.INCREMENTAL);
    }

    /**
     * Validate a proposed allocation without persisting.
     */
    public boolean validateAllocation(ProposedAllocation proposed) {
        return proposed.isValid() && !proposed.getAllocations().isEmpty();
    }

}
