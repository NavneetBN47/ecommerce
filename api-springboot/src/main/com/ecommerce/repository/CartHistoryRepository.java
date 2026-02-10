package com.ecommerce.repository;

import com.ecommerce.entity.CartHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for CartHistory entity
 */
@Repository
public interface CartHistoryRepository extends JpaRepository<CartHistory, UUID> {
    
    List<CartHistory> findByCartIdOrderByPerformedAtDesc(UUID cartId);
    
    List<CartHistory> findByActionType(CartHistory.ActionType actionType);
    
    List<CartHistory> findByPerformedBy(UUID userId);
}