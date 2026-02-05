package com.ecommerce.service;

import com.ecommerce.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled service for cleaning up inactive carts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartCleanupScheduler {

    private final ShoppingCartRepository cartRepository;

    @Value("${app.cart.inactive-days-threshold:30}")
    private int inactiveDaysThreshold;

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupInactiveCarts() {
        log.info("Starting cleanup of inactive carts");
        
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(inactiveDaysThreshold);
        
        int markedCount = cartRepository.markInactiveCartsAsAbandoned(thresholdDate);
        log.info("Marked {} carts as abandoned", markedCount);
        
        // Delete abandoned carts older than threshold
        int deletedCount = cartRepository.deleteAbandonedCarts(
            LocalDateTime.now().minusDays(inactiveDaysThreshold + 7));
        log.info("Deleted {} abandoned carts", deletedCount);
        
        log.info("Completed cleanup of inactive carts");
    }
}