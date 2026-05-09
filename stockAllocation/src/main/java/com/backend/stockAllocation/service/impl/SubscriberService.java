package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.dto.request.RiskProfileReq;
import com.backend.stockAllocation.dto.request.StrategyAllocationRequest;
import com.backend.stockAllocation.dto.request.SubscriberRequest;
import com.backend.stockAllocation.entity.*;
import com.backend.stockAllocation.enums.AllocationRunType;
import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.enums.SubscriberStatus;
import com.backend.stockAllocation.exception.ResourceNotFoundException;
import com.backend.stockAllocation.mapper.dto.AllocationStrategyMapper;
import com.backend.stockAllocation.mapper.dto.response.AllocationStrategyResponseDTO;
import com.backend.stockAllocation.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.hibernate.annotations.AnyDiscriminatorImplicitValues;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final AppUserRepository appUserRepository;
    private final AllocationStrategyMapper allocationStrategyMapper;
    // Onboard a new subscriber with a multi-strategy mix.
    // Strategy allocations must sum to 100%.

    @Transactional
    public Subscriber onboard(SubscriberRequest request) throws BadRequestException {
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        validateStrategyAllocations(request.getStrategyAllocations());

        Subscriber subscriber = Subscriber.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
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

    // Bulk import subscribers.

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


    @Transactional
    public Subscriber updateStrategyMix(Long appUserId, List<StrategyAllocationRequest> newMix) throws BadRequestException {
        Subscriber subscriber = getById(appUserId);
        Long subscriberId=subscriber.getId();
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
    public Subscriber addInflow(Long appUserId, BigDecimal amount) {
        Subscriber subscriber = getById(appUserId);
        Long subscriberId=subscriber.getId();
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
    public Subscriber updateStatus(Long appUserId, SubscriberStatus status) {
        Subscriber subscriber = getById(appUserId);
        subscriber.setStatus(status);
        return subscriberRepository.save(subscriber);
    }

    public Subscriber getById(Long appUserid) {
        String email=appUserRepository.findById(appUserid).get().getEmail();
        Long id=subscriberRepository.findByEmail(email).get().getId();
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

    private void validateStrategyAllocations(List<StrategyAllocationRequest> allocations) throws BadRequestException {
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

    public @Nullable List<AllocationStrategyResponseDTO> getAllStrategies() {



        return allocationStrategyMapper.toResponseDTOList(strategyRepository.findByIsActiveTrue());
    }

    public @Nullable Object getCurrentStrategy(Long id) {

        Long subscriberId=getById(id).getId();
        SubscriberStrategyAllocation st=ssaRepository.findBySubscriberId(subscriberId).get(0);
         Map<String,Object> answer=new HashMap<>();
         answer.put("strategyId",st.getStrategy().getId());
         answer.put("strategyName",st.getStrategy().getName());
         answer.put("risk",st.getStrategy().getRiskProfile());

        return answer;
    }

    public @Nullable Subscriber createSubscriber(@Valid RiskProfileReq profile, Long appUserId) throws BadRequestException {

        AppUser appUser=appUserRepository.findById(appUserId).orElseThrow(()->new ResourceNotFoundException("AppUser",appUserId));

        List<StrategyAllocationRequest> strategyAllocations=new ArrayList<>();
        StrategyAllocationRequest st=new StrategyAllocationRequest();
        if(profile.getRiskProfile().equals("CONSERVATIVE")) {
             st.setStrategyId(1L);
             st.setAllocationPercent(new BigDecimal(100));
        }
        else if(profile.getRiskProfile().equals("MODERATE")) {
            st.setStrategyId(2L);st.setAllocationPercent(new BigDecimal(100));
        }
        else {
            st.setStrategyId(3L);
            st.setAllocationPercent(new BigDecimal(100));
        }
        strategyAllocations.add(st);

        SubscriberRequest request = SubscriberRequest.builder()
                .name(appUser.getFirstName()+" "+appUser.getLastName())
                .email(appUser.getEmail())
                .password(null)
                .investmentAmount(new BigDecimal(0))
                .primaryRiskProfile(RiskProfile.valueOf(profile.getRiskProfile()))
                .strategyAllocations(strategyAllocations)
                .build();
        return onboard(request);
    }
}