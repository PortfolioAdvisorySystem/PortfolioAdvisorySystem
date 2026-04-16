package com.backend.stockAllocation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final AllocationStrategyRepository strategyRepository;
    private final SubscriberStrategyAllocationRepository ssaRepository;
    private final PortfolioRepository portfolioRepository;
    private final AllocationEngine allocationEngine;
    private final AuditService auditService;
    /**
     * Onboard a new subscriber with a multi-strategy mix.
     * Strategy allocations must sum to 100%.
     */
    @Transactional
    public Subscriber onboard(SubscriberRequest request) {
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        validateStrategyAllocations(request.getStrategyAllocations());

        Subscriber subscriber = Subscriber.builder()
                .name(request.getName())
                .email(request.getEmail())
                .investmentAmount(request.getInvestmentAmount())
                .primaryRiskProfile(request.getPrimaryRiskProfile())
                .status(SubscriberStatus.ACTIVE)
                .strategyAllocations(new ArrayList<>())
                .build();
        subscriberRepository.save(subscriber);

        // Create portfolio
        Portfolio portfolio = Portfolio.builder()
                .subscriber(subscriber)
                .unallocatedAmount(BigDecimal.ZERO)
                .positions(new ArrayList<>())
                .build();
        portfolioRepository.save(portfolio);

        // Bind strategy allocations
        bindStrategyAllocations(subscriber, request.getStrategyAllocations());

        // Run initial allocation
        allocationEngine.allocate(subscriber, null, AllocationRunType.INITIAL);

        auditService.log("SUBSCRIBER_ONBOARDED",
                "Subscriber " + subscriber.getId() + " onboarded with " +
                        request.getStrategyAllocations().size() + " strategies",
                "SYSTEM", subscriber.getId(), "Subscriber");

        return subscriber;
    }
    /**
     * Bulk import subscribers.
     */
    @Transactional
    public List<Subscriber> bulkOnboard(List<SubscriberRequest> requests) {
        List<Subscriber> results = new ArrayList<>();
        for (SubscriberRequest req : requests) {
            try {
                results.add(onboard(req));
            } catch (Exception e) {
                log.error("Failed to onboard subscriber {}: {}", req.getEmail(), e.getMessage());
            }
        }
        return results;
    }
    /**
     * Update subscriber's strategy mix (re-binds and triggers rebalance).
     */
    @Transactional
    public Subscriber updateStrategyMix(Long subscriberId, List<StrategyAllocationRequest> newMix) {
        Subscriber subscriber = getById(subscriberId);
        validateStrategyAllocations(newMix);

        // Remove old allocations
        ssaRepository.deleteBySubscriberId(subscriberId);

        // Bind new allocations
        bindStrategyAllocations(subscriber, newMix);

        auditService.log("STRATEGY_MIX_UPDATED",
                "Subscriber " + subscriberId + " strategy mix updated",
                "SYSTEM", subscriberId, "Subscriber");
        return subscriber;
    }
    @Transactional
    public Subscriber addInflow(Long subscriberId, BigDecimal amount) {
        Subscriber subscriber = getById(subscriberId);
        subscriber.setInvestmentAmount(subscriber.getInvestmentAmount().add(amount));

        // Update each slice's allocated amount
        List<SubscriberStrategyAllocation> slices = ssaRepository.findBySubscriberId(subscriberId);
        for (SubscriberStrategyAllocation slice : slices) {
            BigDecimal newAllocated = subscriber.getInvestmentAmount()
                    .multiply(slice.getAllocationPercent())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            slice.setAllocatedAmount(newAllocated);
            ssaRepository.save(slice);
        }

        subscriberRepository.save(subscriber);
        allocationEngine.applyInflows(subscriber, amount);

        auditService.log("INFLOW_APPLIED",
                "Subscriber " + subscriberId + " inflow=" + amount,
                "SYSTEM", subscriberId, "Subscriber");
        return subscriber;
    }
    @Transactional
    public Subscriber updateStatus(Long subscriberId, SubscriberStatus status) {
        Subscriber subscriber = getById(subscriberId);
        subscriber.setStatus(status);
        return subscriberRepository.save(subscriber);
    }

    public Subscriber getById(Long id) {
        return subscriberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber", id));
    }

    public List<Subscriber> getAll() {
        return subscriberRepository.findAll();
    }

    public List<Subscriber> getByStatus(SubscriberStatus status) {
        return subscriberRepository.findByStatus(status);
    }

    private void bindStrategyAllocations(Subscriber subscriber, List<StrategyAllocationRequest> requests) {
        for (StrategyAllocationRequest req : requests) {
            AllocationStrategy strategy = strategyRepository.findById(req.getStrategyId())
                    .orElseThrow(() -> new ResourceNotFoundException("AllocationStrategy", req.getStrategyId()));

            BigDecimal allocated = subscriber.getInvestmentAmount()
                    .multiply(req.getAllocationPercent())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            SubscriberStrategyAllocation ssa = SubscriberStrategyAllocation.builder()
                    .subscriber(subscriber)
                    .strategy(strategy)
                    .allocationPercent(req.getAllocationPercent())
                    .allocatedAmount(allocated)
                    .build();
            ssaRepository.save(ssa);
        }
    }

    private void validateStrategyAllocations(List<StrategyAllocationRequest> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            throw new BadRequestException("At least one strategy allocation is required");
        }
        BigDecimal total = allocations.stream()
                .map(StrategyAllocationRequest::getAllocationPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            throw new BadRequestException("Strategy allocation percentages must sum to 100. Got: " + total);
        }
    }

}
