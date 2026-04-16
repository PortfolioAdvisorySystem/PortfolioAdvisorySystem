package com.backend.stockAllocation.strategy;

import com.backend.stockAllocation.enums.RiskProfile;
import com.backend.stockAllocation.strategy.impl.AggressiveStrategy;
import com.backend.stockAllocation.strategy.impl.ConservativeStrategy;
import com.backend.stockAllocation.strategy.impl.ModerateStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyResolver {

    private final ConservativeStrategy conservativeStrategy;
    private final ModerateStrategy moderateStrategy;
    private final AggressiveStrategy aggressiveStrategy;

    public AllocationStrategyInterface resolve(RiskProfile profile) {
        return switch (profile) {
            case CONSERVATIVE -> conservativeStrategy;
            case MODERATE     -> moderateStrategy;
            case AGGRESSIVE   -> aggressiveStrategy;
        };
    }
}
