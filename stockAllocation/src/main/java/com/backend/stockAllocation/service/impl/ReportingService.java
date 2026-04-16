package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.MigrationRecord;
import com.backend.stockAllocation.entity.Portfolio;
import com.backend.stockAllocation.entity.Position;
import com.backend.stockAllocation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ReportingService {
    private final SubscriberRepository subscriberRepository;
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final MigrationRecordRepository migrationRecordRepository;
    private final AllocationDecisionRepository allocationDecisionRepository;
    private final RebalanceEventRepository rebalanceEventRepository;

    /** Current allocation grouped by subscriber */
    public Map<String, Object> getAllocationBySubscriber(Long subscriberId) {
        Portfolio portfolio = portfolioRepository.findBySubscriberId(subscriberId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for subscriber " + subscriberId));

        List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId());
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("subscriberId", subscriberId);
        report.put("totalAUM", portfolio.getSubscriber().getInvestmentAmount());
        report.put("unallocated", portfolio.getUnallocatedAmount());

        List<Map<String, Object>> positionList = positions.stream()
                .filter(p -> !p.isMarkedForDeallocation())
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("stock", p.getStock().getSymbol());
                    m.put("sector", p.getStock().getSector());
                    m.put("strategy", p.getStrategy() != null ? p.getStrategy().getName() : "N/A");
                    m.put("weight", p.getWeight());
                    return m;
                }).collect(Collectors.toList());

        report.put("positions", positionList);
        return report;
    }

    /** Stocks currently marked for deallocation */
    public List<Map<String, Object>> getDeallocationQueue() {
        return positionRepository.findByMarkedForDeallocationTrue().stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("positionId", p.getId());
                    m.put("stock", p.getStock().getSymbol());
                    m.put("subscriberId", p.getPortfolio().getSubscriber().getId());
                    m.put("amount", p.getWeight());
                    m.put("reason", p.getDeallocationReason());
                    return m;
                }).collect(Collectors.toList());
    }

    /** Full migration history */
    public List<MigrationRecord> getMigrationHistory() {
        return migrationRecordRepository.findAll();
    }

    /** Migration history for a specific subscriber */
    public List<MigrationRecord> getMigrationHistoryForSubscriber(Long subscriberId) {
        return migrationRecordRepository.findBySubscriberId(subscriberId);
    }

    /** Rule breach summary: positions that were deallocated grouped by reason */
    public Map<String, Long> getRuleBreachSummary() {
        return positionRepository.findByMarkedForDeallocationTrue().stream()
                .filter(p -> p.getDeallocationReason() != null)
                .collect(Collectors.groupingBy(Position::getDeallocationReason, Collectors.counting()));
    }

    /** Allocation summary across all strategies */
    public Map<String, Object> getAllocationByStrategy() {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Position> all = positionRepository.findAll().stream()
                .filter(p -> !p.isMarkedForDeallocation()).toList();

        Map<String, BigDecimal> byStrategy = all.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStrategy() != null ? p.getStrategy().getName() : "Unassigned",
                        Collectors.reducing(BigDecimal.ZERO, Position::getWeight, BigDecimal::add)));

        report.put("allocationByStrategy", byStrategy);
        report.put("totalPositions", all.size());
        return report;
    }

    /** Before/after comparison — uses allocation decisions for a run */
    public Map<String, Object> getRebalanceImpact(String runId) {
        List<AllocationDecision> decisions = allocationDecisionRepository.findByRunId(runId);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("runId", runId);
        report.put("decisionsCount", decisions.size());
        report.put("decisions", decisions.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("subscriber", d.getSubscriber().getId());
            m.put("stock", d.getStock().getSymbol());
            m.put("amount", d.getAmountAllocated());
            m.put("weightPct", d.getWeightPercent());
            m.put("runType", d.getRunType());
            return m;
        }).collect(Collectors.toList()));
        return report;
    }

    /** Unallocated pool across all subscribers */
    public List<Map<String, Object>> getUnallocatedPool() {
        return portfolioRepository.findAll().stream()
                .filter(p -> p.getUnallocatedAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("subscriberId", p.getSubscriber().getId());
                    m.put("unallocatedAmount", p.getUnallocatedAmount());
                    return m;
                }).collect(Collectors.toList());
    }

    /** Rebalance event history */
    public List<RebalanceEvent> getRebalanceHistory() {
        return rebalanceEventRepository.findByOrderByTriggeredAtDesc();
    }
}
