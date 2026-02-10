package com.ecommerce.repository;

import com.ecommerce.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ShoppingCart entity
 * Provides database operations for cart management with lazy creation and auto-deletion
 */
@Repository
public interface CartRepository extends JpaRepository<ShoppingCart, Long> {

    /**
     * Find cart by user ID
     * Used for lazy cart creation - returns empty if cart doesn't exist
     */
    Optional<ShoppingCart> findByUserId(Long userId);

    /**
     * Delete cart by user ID
     * Used for logout cleanup
     */
    @Modifying
    @Query("DELETE FROM ShoppingCart c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Check if user has a cart
     */
    boolean existsByUserId(Long userId);

    /**
     * Find cart with items by user ID
     */
    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<ShoppingCart> findByUserIdWithItems(@Param("userId") Long userId);
}