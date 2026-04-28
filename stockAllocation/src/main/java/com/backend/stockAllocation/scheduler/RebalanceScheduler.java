package com.backend.stockAllocation.scheduler;
import com.backend.stockAllocation.service.impl.RebalanceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RebalanceScheduler {

    private final RebalanceManager rebalanceManager;


     // Run scheduled rebalance every day at midnight.
     // Cron: 0 0 0 * * ? = every day at 00:00:00

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledRebalance() {
        log.info("Scheduler triggered rebalance");
        try {
            rebalanceManager.runScheduledRebalance();
        } catch (Exception e) {
            log.error("Scheduled rebalance failed: {}", e.getMessage(), e);
        }
    }
}
