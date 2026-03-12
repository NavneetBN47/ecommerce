package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Cart Repository
 * Handles database operations for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.userId = :userId")
    Optional<Cart> findByUserUserId(@Param("userId") Long userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUserUserId(Long userId);

    /**
     * Delete cart by user ID
     */
    void deleteByUserUserId(Long userId);
}