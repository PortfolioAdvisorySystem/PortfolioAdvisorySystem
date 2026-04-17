package com.backend.stockAllocation.config;

import com.backend.stockAllocation.entity.AllocationStrategy;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.entity.Stock;
import com.backend.stockAllocation.enums.RiskCategory;
import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.enums.RuleType;
import com.backend.stockAllocation.repository.AllocationStrategyRepository;
import com.backend.stockAllocation.repository.RuleRepository;
import com.backend.stockAllocation.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AllocationStrategyRepository strategyRepository;
    private final StockRepository stockRepository;
    private final RuleRepository ruleRepository;

    @Override
    public void run(String... args) {
        if (strategyRepository.count() == 0) {
            seedStrategies();
        }
        if (stockRepository.count() == 0) {
            seedStocks();
        }
        if (ruleRepository.count() == 0) {
            seedRules();
        }
        log.info("Data initialization complete. Strategies={}, Stocks={}, Rules={}",
                strategyRepository.count(), stockRepository.count(), ruleRepository.count());
    }

    private void seedStrategies() {
        strategyRepository.saveAll(List.of(
                AllocationStrategy.builder()
                        .name("Conservative Plan")
                        .riskProfile(RiskProfile.CONSERVATIVE)
                        .maxStockConcentration(new BigDecimal("15.00"))
                        .maxSectorExposure(new BigDecimal("25.00"))
                        .cashBufferPercent(new BigDecimal("10.00"))
                        .isActive(true).build(),

                AllocationStrategy.builder()
                        .name("Moderate Plan")
                        .riskProfile(RiskProfile.MODERATE)
                        .maxStockConcentration(new BigDecimal("20.00"))
                        .maxSectorExposure(new BigDecimal("35.00"))
                        .cashBufferPercent(new BigDecimal("5.00"))
                        .isActive(true).build(),

                AllocationStrategy.builder()
                        .name("Aggressive Plan")
                        .riskProfile(RiskProfile.AGGRESSIVE)
                        .maxStockConcentration(new BigDecimal("30.00"))
                        .maxSectorExposure(new BigDecimal("50.00"))
                        .cashBufferPercent(new BigDecimal("2.00"))
                        .isActive(true).build()
        ));
        log.info("Seeded 3 allocation strategies");
    }

    private void seedStocks() {
        stockRepository.saveAll(List.of(
                // LOW risk - TECHNOLOGY
                buildStock("AAPL", "TECHNOLOGY", "Large Cap", 90, "5000000", "3000000000000", RiskCategory.LOW),
                buildStock("MSFT", "TECHNOLOGY", "Large Cap", 88, "4500000", "2800000000000", RiskCategory.LOW),
                buildStock("GOOGL", "TECHNOLOGY", "Large Cap", 85, "2000000", "1900000000000", RiskCategory.LOW),

                // MEDIUM risk - TECHNOLOGY
                buildStock("META", "TECHNOLOGY", "Large Cap", 78, "2500000", "1100000000000", RiskCategory.MEDIUM),
                buildStock("AMD",  "TECHNOLOGY", "Mid Cap",   72, "1500000", "250000000000",  RiskCategory.MEDIUM),

                // HIGH risk - TECHNOLOGY
                buildStock("NVDA", "TECHNOLOGY", "Large Cap", 80, "3000000", "2200000000000", RiskCategory.HIGH),
                buildStock("TSLA", "TECHNOLOGY", "Large Cap", 75, "2200000", "800000000000",  RiskCategory.HIGH),

                // LOW risk - FINANCE
                buildStock("JPM",  "FINANCE", "Large Cap", 82, "1800000", "500000000000", RiskCategory.LOW),
                buildStock("BAC",  "FINANCE", "Large Cap", 80, "2000000", "320000000000", RiskCategory.LOW),

                // MEDIUM risk - FINANCE
                buildStock("GS",   "FINANCE", "Large Cap", 70, "900000", "130000000000", RiskCategory.MEDIUM),
                buildStock("MS",   "FINANCE", "Large Cap", 68, "850000", "150000000000", RiskCategory.MEDIUM),

                // HIGH risk - FINANCE
                buildStock("COIN", "FINANCE", "Mid Cap",   55, "500000", "40000000000",  RiskCategory.HIGH),

                // LOW risk - HEALTHCARE
                buildStock("JNJ",  "HEALTHCARE", "Large Cap", 84, "1200000", "410000000000", RiskCategory.LOW),
                buildStock("PFE",  "HEALTHCARE", "Large Cap", 81, "1500000", "170000000000", RiskCategory.LOW),

                // MEDIUM risk - HEALTHCARE
                buildStock("MRNA", "HEALTHCARE", "Mid Cap",   65, "800000", "55000000000",  RiskCategory.MEDIUM),

                // HIGH risk - ENERGY
                buildStock("XOM",  "ENERGY", "Large Cap", 73, "1400000", "450000000000", RiskCategory.MEDIUM),
                buildStock("CVX",  "ENERGY", "Large Cap", 71, "1100000", "320000000000", RiskCategory.MEDIUM)
        ));
        log.info("Seeded 17 stocks");
    }

    private Stock buildStock(String symbol, String sector, String category,
                             int liquidity, String volume, String cap, RiskCategory risk) {
        return Stock.builder()
                .symbol(symbol).sector(sector).category(category)
                .liquidityScore(liquidity)
                .avgVolume(new BigDecimal(volume))
                .marketCap(new BigDecimal(cap))
                .riskCategory(risk)
                .isActive(true).isSuspended(false).isBlacklisted(false)
                .build();
    }

    private void seedRules() {
        LocalDate today = LocalDate.now();
        LocalDate farFuture = today.plusYears(10);

        ruleRepository.saveAll(List.of(
                // Minimum volume: stocks must have avg volume >= 500,000
                Rule.builder().name("Min Volume Rule").ruleType(RuleType.MINIMUM_VOLUME)
                        .threshold(new BigDecimal("500000")).priority(100)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Stock must have minimum average daily trading volume of 500,000").build(),

                // Min liquidity score >= 50
                Rule.builder().name("Min Liquidity Rule").ruleType(RuleType.MINIMUM_LIQUIDITY)
                        .threshold(new BigDecimal("50")).priority(95)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Stock must have a minimum liquidity score of 50").build(),

                // Max single stock concentration 30% of total AUM
                Rule.builder().name("Max Concentration Rule").ruleType(RuleType.MAXIMUM_CONCENTRATION)
                        .threshold(new BigDecimal("30")).priority(90)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("No stock may exceed 30% of total subscriber AUM").build(),

                // Max sector exposure 50% of total AUM
                Rule.builder().name("Sector Cap Rule").ruleType(RuleType.SECTOR_CAP)
                        .threshold(new BigDecimal("50")).priority(85)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Total allocation to any single sector must not exceed 50% of AUM").build(),

                // Risk profile compatibility
                Rule.builder().name("Risk Profile Compatibility").ruleType(RuleType.RISK_PROFILE_COMPATIBILITY)
                        .threshold(BigDecimal.ONE).priority(99)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Stock risk category must be compatible with subscriber risk profile").build(),

                // Suspended instruments cannot be allocated
                Rule.builder().name("Suspended Instrument Rule").ruleType(RuleType.SUSPENDED_INSTRUMENT)
                        .threshold(BigDecimal.ONE).priority(110)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Suspended or inactive stocks are not eligible for allocation").build(),

                // Blacklist rule
                Rule.builder().name("Blacklist Rule").ruleType(RuleType.BLACKLIST)
                        .threshold(BigDecimal.ONE).priority(115)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Blacklisted stocks cannot be allocated").build(),

                // Minimum investable amount: 1000
                Rule.builder().name("Min Investable Amount").ruleType(RuleType.MINIMUM_INVESTABLE_AMOUNT)
                        .threshold(new BigDecimal("1000")).priority(80)
                        .effectiveDate(today).expiryDate(farFuture).isActive(true).version(1)
                        .description("Minimum amount that can be allocated to any single stock is 1000").build()
        ));
        log.info("Seeded 8 rules");
    }
}
