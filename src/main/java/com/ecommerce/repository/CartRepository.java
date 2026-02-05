package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Cart entity
 * Provides database access methods for cart operations
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Find active cart by user ID
     * Business Rule: One active cart per user
     * @param userId the user ID
     * @return Optional containing cart if found
     */
    Optional<Cart> findByUserIdAndStatus(UUID userId, String status);

    /**
     * Delete cart by user ID and status
     * Used during logout to cleanup cart
     * @param userId the user ID
     * @param status the cart status
     */
    void deleteByUserIdAndStatus(UUID userId, String status);

    /**
     * Check if user has active cart
     * @param userId the user ID
     * @param status the cart status
     * @return true if exists, false otherwise
     */
    boolean existsByUserIdAndStatus(UUID userId, String status);
}