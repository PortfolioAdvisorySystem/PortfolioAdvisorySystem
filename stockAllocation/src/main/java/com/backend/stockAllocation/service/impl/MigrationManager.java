package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationManager {

    private final StockRepository stockRepository;
    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;
    private final MigrationRecordRepository migrationRecordRepository;
    private final RuleRepository ruleRepository;
    private final RuleEvaluator ruleEvaluator;
    private final AuditService auditService;

    /**
     * Find eligible replacement stocks for a given source stock within a strategy context.
     * Ranks by liquidity score descending then volume descending.
     */
    public List<Stock> findReplacements(Stock sourceStock, Subscriber subscriber,
                                        AllocationStrategy strategy, Portfolio portfolio) {
        List<Stock> candidates = stockRepository.findByIsActiveTrueAndIsSuspendedFalseAndIsBlacklistedFalse()
                .stream()
                .filter(s -> !s.getId().equals(sourceStock.getId()))
                .collect(Collectors.toList());

        List<Rule> activeRules = ruleRepository.findCurrentlyActiveRules(LocalDate.now());
        BigDecimal totalAllocated = positionRepository.sumTotalAllocated(portfolio.getId());

        List<Stock> eligible = new ArrayList<>();
        for (Stock candidate : candidates) {
            // Same or compatible sector preferred but not mandatory
            // Must pass all active rules
            BigDecimal currentSectorWeight = positionRepository.sumWeightBySector(
                    portfolio.getId(), candidate.getSector());
            long subCount = stockRepository.countSubscribersHoldingStock(candidate.getId());

            RuleEvaluationContext ctx = RuleEvaluationContext.builder()
                    .subscriber(subscriber)
                    .stock(candidate)
                    .strategy(strategy)
                    .portfolio(portfolio)
                    .currentStockWeight(BigDecimal.ZERO)
                    .currentSectorWeight(currentSectorWeight != null ? currentSectorWeight : BigDecimal.ZERO)
                    .totalAllocated(totalAllocated != null ? totalAllocated : BigDecimal.ZERO)
                    .totalAUM(subscriber.getInvestmentAmount())
                    .subscriberCountForStock(subCount)
                    .stockMetrics(Collections.emptyMap())
                    .proposedAmount(BigDecimal.ZERO)
                    .build();

            StockEligibilityResult result = ruleEvaluator.checkWithRules(activeRules, ctx);
            if (result.isEligible()) {
                eligible.add(candidate);
            }
        }

        // Sort: same sector first, then by liquidity score desc
        eligible.sort(Comparator
                .<Stock, Integer>comparing(s -> s.getSector().equals(sourceStock.getSector()) ? 0 : 1)
                .thenComparing(s -> -(s.getLiquidityScore() != null ? s.getLiquidityScore() : 0))
                .thenComparing(s -> s.getAvgVolume().negate()));

        return eligible;
    }

    /**
     * Execute migration from deallocated positions to replacement stocks.
     * Supports one-to-one and one-to-many (weighted redistribution).
     */
    @Transactional
    public List<MigrationRecord> executeMigration(List<Position> deallocatedPositions,
                                                  Map<Stock, List<Stock>> replacementMap) {
        List<MigrationRecord> records = new ArrayList<>();

        for (Position position : deallocatedPositions) {
            Portfolio portfolio = position.getPortfolio();
            Subscriber subscriber = portfolio.getSubscriber();
            Stock sourceStock = position.getStock();
            BigDecimal amountToMigrate = position.getWeight();
            List<Stock> replacements = replacementMap.getOrDefault(sourceStock, Collections.emptyList());

            if (replacements.isEmpty()) {
                // Fallback: leave in unallocated pool
                portfolio.setUnallocatedAmount(portfolio.getUnallocatedAmount().add(amountToMigrate));
                portfolioRepository.save(portfolio);
                log.warn("No replacements for stock {} subscriber {} – moved to unallocated pool",
                        sourceStock.getSymbol(), subscriber.getId());

                MigrationRecord fallback = MigrationRecord.builder()
                        .sourceStock(sourceStock)
                        .targetStock(null)
                        .subscriber(subscriber)
                        .amountShifted(amountToMigrate)
                        .migrationReason("No eligible replacement found – moved to unallocated")
                        .ruleTrigger(position.getDeallocationReason())
                        .build();
                migrationRecordRepository.save(fallback);
                records.add(fallback);
                continue;
            }

            // Weighted redistribution: equal share across replacements (can be extended)
            int count = replacements.size();
            BigDecimal perReplacement = amountToMigrate
                    .divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);

            for (Stock target : replacements) {
                upsertPosition(portfolio, target, position.getStrategy(), perReplacement);

                MigrationRecord record = MigrationRecord.builder()
                        .sourceStock(sourceStock)
                        .targetStock(target)
                        .subscriber(subscriber)
                        .amountShifted(perReplacement)
                        .migrationReason("Deallocated from " + sourceStock.getSymbol() +
                                " → reallocated to " + target.getSymbol())
                        .ruleTrigger(position.getDeallocationReason())
                        .build();
                migrationRecordRepository.save(record);
                records.add(record);
            }

            // Remove the old position
            portfolio.getPositions().remove(position);
            positionRepository.delete(position);
            portfolioRepository.save(portfolio);

            auditService.log("MIGRATION_EXECUTED",
                    "Source=" + sourceStock.getSymbol() + " targets=" + replacements.size() +
                            " amount=" + amountToMigrate,
                    "SYSTEM", subscriber.getId(), "Subscriber");
        }
        return records;
    }



    private void upsertPosition(Portfolio portfolio, Stock stock,
                                AllocationStrategy strategy, BigDecimal amount) {
        Optional<Position> existing = positionRepository
                .findByPortfolioIdAndStockIdAndStrategyId(
                        portfolio.getId(), stock.getId(),
                        strategy != null ? strategy.getId() : null);

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
                    .quantity(BigDecimal.ONE)
                    .purchasePrice(BigDecimal.ONE)
                    .build();
            positionRepository.save(pos);
        }
    }

    public void recordMigration(Stock source, Stock target, String reason,
                                Subscriber subscriber, BigDecimal amount) {
        MigrationRecord record = MigrationRecord.builder()
                .sourceStock(source)
                .targetStock(target)
                .subscriber(subscriber)
                .amountShifted(amount)
                .migrationReason(reason)
                .build();
        migrationRecordRepository.save(record);
    }
}
