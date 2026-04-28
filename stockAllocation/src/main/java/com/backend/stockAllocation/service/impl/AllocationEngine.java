package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.enums.AllocationRunType;
import com.backend.stockAllocation.mapper.dto.AllocationDecisionMapper;
import com.backend.stockAllocation.mapper.dto.response.AllocationDecisionResponseDTO;
import com.backend.stockAllocation.repository.*;
import com.backend.stockAllocation.rule.engine.RuleEvaluationContext;
import com.backend.stockAllocation.rule.engine.RuleEvaluator;
import com.backend.stockAllocation.rule.engine.StockEligibilityResult;
import com.backend.stockAllocation.strategy.AllocationStrategyInterface;
import com.backend.stockAllocation.strategy.ProposedAllocation;
import com.backend.stockAllocation.strategy.StrategyResolver;
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
    private final AllocationDecisionMapper allocationDecisionMapper;

     //Allocate for a single subscriber across all their strategy slices.

    @Transactional
    public List<AllocationDecisionResponseDTO> allocate(Subscriber subscriber,
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
            return Collections.emptyList();
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



        List<AllocationDecisionResponseDTO>ans=allocationDecisionMapper.toResponseDTOList(decisions);
        return ans;
    }



    private List<AllocationDecision> allocateForSlice(
            Subscriber subscriber,
            Portfolio portfolio,
            AllocationStrategy strategyConfig,
            BigDecimal investableAmount,
            String runId,
            AllocationRunType runType) {

        List<Stock> allStocks = stockRepository
                .findByIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse();

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
            log.warn("Proposed allocation invalid for strategy {}: {}",
                    strategyConfig.getName(), proposed.getValidationMessages());
            return Collections.emptyList();
        }

        List<AllocationDecision> decisions = new ArrayList<>();

        // Fix 3: track rejected amounts so unallocated stays accurate
        BigDecimal rejectedAmount = BigDecimal.ZERO;

        for (Map.Entry<Stock, BigDecimal> entry : proposed.getAllocations().entrySet()) {
            Stock stock = entry.getKey();
            BigDecimal amount = entry.getValue();

            if (amount.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Fetch current weights for final rule validation
            BigDecimal currentStockWeight = positionRepository
                    .sumWeightByStock(portfolio.getId(), stock.getId());
            BigDecimal currentSectorWeight = positionRepository
                    .sumWeightBySector(portfolio.getId(), stock.getSector());
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
                log.warn("Final validation failed for stock {}: {}",
                        stock.getSymbol(), validation.getSummary());
                // Fix 3: accumulate rejected amounts instead of silently losing them
                rejectedAmount = rejectedAmount.add(amount);
                continue;
            }

            // Fix 1 & 2: upsertPosition now correctly accumulates weight
            upsertPosition(portfolio, stock, strategyConfig, amount);

            // Fix 3: keep totalAllocated fresh so later stocks in this
            // loop see an accurate running total during rule validation
            totalAllocated = totalAllocated.add(amount);

            // Record decision
            BigDecimal weightPct = amount
                    .divide(subscriber.getInvestmentAmount(), 6, RoundingMode.HALF_UP)
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

        // Fix 3: unallocated = what strategy couldn't place + what rules rejected
        // also accumulate on top of existing unallocated across multiple inflow runs
        BigDecimal currentUnallocated = portfolio.getUnallocatedAmount() != null
                ? portfolio.getUnallocatedAmount()
                : BigDecimal.ZERO;

        portfolio.setUnallocatedAmount(
                currentUnallocated
                        .add(proposed.getUnallocated())
                        .add(rejectedAmount)
        );
        portfolioRepository.save(portfolio);

        return decisions;
    }
    private void upsertPosition(Portfolio portfolio, Stock stock,
                                AllocationStrategy strategy, BigDecimal amount) {
        Optional<Position> existing = positionRepository
                .findByPortfolioIdAndStockIdAndStrategyId(
                        portfolio.getId(), stock.getId(), strategy.getId());

        if (existing.isPresent()) {
            // Fix 2: accumulate weight on top of existing, don't replace it
            Position pos = existing.get();
            pos.setWeight(pos.getWeight().add(amount));
            positionRepository.save(pos);
        } else {
            // Fix 1: removed the broken existing.get() call on an empty Optional
            Position pos = Position.builder()
                    .portfolio(portfolio)
                    .stock(stock)
                    .strategy(strategy)
                    .weight(amount)
                    .quantity(BigDecimal.ONE)
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


     // Apply inflows to subscriber portfolio.

    @Transactional
    public List<AllocationDecisionResponseDTO> applyInflows(Subscriber subscriber, BigDecimal inflows) {
        return allocate(subscriber, inflows, AllocationRunType.INCREMENTAL);
    }


     //Validate a proposed allocation without persisting.

    public boolean validateAllocation(ProposedAllocation proposed) {
        return proposed.isValid() && !proposed.getAllocations().isEmpty();
    }

}
