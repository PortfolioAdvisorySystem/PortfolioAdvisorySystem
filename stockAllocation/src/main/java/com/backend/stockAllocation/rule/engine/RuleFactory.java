package com.backend.stockAllocation.rule.engine;
import org.springframework.stereotype.Component;
import com.backend.stockAllocation.entity.Rule;
import com.backend.stockAllocation.rule.impl.*;


 //Factory that maps a persisted Rule entity to its concrete implementation.
 // Add new rule types here without touching any other class.

@Component
public class RuleFactory {
    public BaseRule create(Rule rule) {
        return switch (rule.getRuleType()) {
            case MINIMUM_VOLUME            -> new MinimumVolumeRule(rule);
            case SECTOR_CAP                -> new SectorCapRule(rule);
            case MAXIMUM_CONCENTRATION     -> new ConcentrationRule(rule);
            case MINIMUM_SUBSCRIBER_COUNT  -> new MinimumSubscriberCountRule(rule);
            case MINIMUM_LIQUIDITY,
                 MINIMUM_LIQUORABILITY     -> new MinimumLiquidityRule(rule);
            case RISK_PROFILE_COMPATIBILITY -> new RiskProfileCompatibilityRule(rule);
            case BLACKLIST                  -> new BlacklistRule(rule);
            case SUSPENDED_INSTRUMENT      -> new SuspendedInstrumentRule(rule);
            case MINIMUM_INVESTABLE_AMOUNT  -> new MinimumInvestableAmountRule(rule);
            case HOLD_FREEZE               -> new HoldFreezeRule(rule);
        };
    }
}

