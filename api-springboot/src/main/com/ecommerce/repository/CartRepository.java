package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Cart entity
 * Business Rule: One cart per user
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    /**
     * Find cart by user ID
     * Business Rule: One cart per user
     * @param userId user's UUID
     * @return Optional containing cart if exists
     */
    Optional<Cart> findByUserId(UUID userId);
    
    /**
     * Find cart by session ID (for guest users)
     * @param sessionId session identifier
     * @return Optional containing cart if exists
     */
    Optional<Cart> findBySessionId(String sessionId);
    
    /**
     * Delete cart by user ID
     * Used during logout cleanup
     * @param userId user's UUID
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
    
    /**
     * Check if user has an active cart
     * @param userId user's UUID
     * @return true if cart exists
     */
    boolean existsByUserId(UUID userId);
}