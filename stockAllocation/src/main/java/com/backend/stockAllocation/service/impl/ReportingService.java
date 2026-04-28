package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.mapper.dto.PositionMapper;
import com.backend.stockAllocation.mapper.dto.response.PositionResponseDto;
import com.backend.stockAllocation.mapper.dto.response.StrategyAllocationDTO;
import com.backend.stockAllocation.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
    private final PositionMapper positionMapper;
    private final RuleRepository ruleRepository;
    private final StockRepository stockRepository;
    private final AllocationEngine allocationEngine;
    // Current allocation grouped by subscriber
    public Map<String, Object> getAllocationBySubscriber(Long subscriberId) {
        Portfolio portfolio = portfolioRepository.findBySubscriberId(subscriberId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for subscriber " + subscriberId));

        List<Position> positions_repo = positionRepository.findByPortfolioId(portfolio.getId());
        List<PositionResponseDto>positions=positionMapper.toResponseDTOList(positions_repo);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("subscriberId", subscriberId);
        report.put("totalAUM", portfolio.getSubscriber().getInvestmentAmount());
        report.put("unallocated", portfolio.getUnallocatedAmount());

        List<Map<String, Object>> positionList = positions.stream()
                .filter(p -> !p.isMarkedForDeallocation())
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("stock", p.getStockSymbol());
                    m.put("sector", p.getSector());
                    m.put("strategy", p.getStrategyName() != null ? p.getStrategyName() : "N/A");
                    m.put("weight", p.getWeight());
                    m.put("purchasePrice",p.getPurchasePrice());
                    m.put("quantity",p.getQuantity());
                    m.put("riskCategory",p.getRiskProfile());
                    return m;
                }).collect(Collectors.toList());

        report.put("positions", positionList);
        return report;
    }

    // Stocks currently marked for deallocation
    public List<Map<String, Object>> getDeallocationQueue() {

        List<PositionResponseDto>list=positionMapper.toResponseDTOList(positionRepository.findByMarkedForDeallocationTrue());
        return list.stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("positionId", p.getId());
                    m.put("stock", p.getStockSymbol());
                    m.put("subscriberId", p.getSubscriberId());
                    m.put("amount", p.getWeight());
                    m.put("reason", p.getDeallocationReason());
                    return m;
                }).collect(Collectors.toList());
    }

    // Full migration history
    public List<MigrationRecord> getMigrationHistory() {
        return migrationRecordRepository.findAll();
    }

    // Migration history for a specific subscriber
    public List<MigrationRecord> getMigrationHistoryForSubscriber(Long subscriberId) {
        return migrationRecordRepository.findBySubscriberId(subscriberId);
    }

    // Rule breach summary: positions that were deallocated grouped by reason
    public Map<String, Long> getRuleBreachSummary() {
        return positionRepository.findByMarkedForDeallocationTrue().stream()
                .filter(p -> p.getDeallocationReason() != null)
                .collect(Collectors.groupingBy(Position::getDeallocationReason, Collectors.counting()));
    }

//    // Allocation summary across all strategies
//    public Map<String, Object> getAllocationByStrategy() {
//        Map<String, Object> report = new LinkedHashMap<>();
//        List<Position> all = positionRepository.findAll().stream()
//                .filter(p -> !p.isMarkedForDeallocation()).toList();
//
//        Map<String, BigDecimal> byStrategy = all.stream()
//                .collect(Collectors.groupingBy(
//                        p -> p.getStrategy() != null ? p.getStrategy().getName() : "Unassigned",
//                        Collectors.reducing(BigDecimal.ZERO, Position::getWeight, BigDecimal::add)));
//
//        report.put("allocationByStrategy", byStrategy);
//        report.put("totalPositions", all.size());
//        return report;
//    }
public List<StrategyAllocationDTO> getAllocationByStrategy() {

    List<Position> all = positionRepository.findAll().stream()
            .filter(p -> !p.isMarkedForDeallocation())
            .toList();

    // 🔹 total allocation
    BigDecimal total = all.stream()
            .map(Position::getWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 🔹 group by strategy
    Map<String, List<Position>> grouped = all.stream()
            .collect(Collectors.groupingBy(
                    p -> p.getStrategy() != null
                            ? p.getStrategy().getName()
                            : "Unassigned"
            ));

    // 🔹 convert to DTO list
    return grouped.entrySet().stream()
            .map(entry -> {

                String strategyName = entry.getKey();
                List<Position> positions = entry.getValue();

                BigDecimal sum = positions.stream()
                        .map(Position::getWeight)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                long count = positions.size();

                BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                        ? sum.multiply(BigDecimal.valueOf(100))
                        .divide(total, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                return StrategyAllocationDTO.builder()
                        .strategyName(strategyName)
                        .totalAllocation(sum)
                        .percentage(percentage)
                        .positionCount(count)
                        .build();
            })
            .toList();
}
    // Before after comparison — uses allocation decisions for a run
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

    // Unallocated pool across all subscribers
    public List<Map<String, Object>> getUnallocatedPool() {
        return portfolioRepository.findAll().stream()
                .filter(p -> p.getUnallocatedAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("subscriberId", p.getSubscriber().getId());
                    m.put("unallocatedAmount", p.getUnallocatedAmount());
                    m.put("allocatedAmount",p.getSubscriber().getAUM().subtract(p.getUnallocatedAmount()));
                    return m;
                }).collect(Collectors.toList());
    }

    // Rebalance event history
    public List<RebalanceEvent> getRebalanceHistory() {
        return rebalanceEventRepository.findByOrderByTriggeredAtDesc();
    }

    public @Nullable List<Map<String, Object>> getSubscriberReport() {

        List<Subscriber>subscribers=subscriberRepository.findAll();

        List<Map<String,Object>>ans=new ArrayList<>();

        for(Subscriber sub:subscribers)
        {
            Map<String,Object>m=new HashMap<>();
            m.put("subscriberId","SUB "+sub.getId());
            m.put("name",sub.getName());
            m.put("riskProfile",sub.getPrimaryRiskProfile());
            m.put("aum",sub.getAUM());
            m.put("status",sub.getStatus());
            ans.add(m);
        }
        return ans;
    }

    public @Nullable Map<String, Object> dashboardSummary() {

        Map<String, Object> response = new HashMap<>();

        response.put("totalSubscribers", subscriberRepository.count());
        response.put("activeRules", ruleRepository.countByIsActiveTrue());
        response.put("eligibleStocks", stockRepository.countByIsActiveTrue());
        //response.put("pendingApprovals", workflowRepository.countPending());

        response.put("allocationByStrategy", this.getAllocationByStrategy());
        response.put("ruleBreaches", this.getRuleBreachSummary());

        return response;
    }
}
