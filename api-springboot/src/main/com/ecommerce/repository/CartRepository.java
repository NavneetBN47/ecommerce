package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Cart Repository
 * Data access layer for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     * @param userId the user ID
     * @return Optional containing cart if found
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Delete cart by user ID
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Check if cart exists for user
     * @param userId the user ID
     * @return true if cart exists
     */
    boolean existsByUserId(Long userId);
}