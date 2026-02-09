package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Cart entity
 * Provides database access for cart operations per LLD
 * Implements one cart per user constraint
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Find cart by user (one cart per user per LLD)
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(UUID userId);

    /**
     * Delete cart by user (for logout cleanup per LLD)
     */
    void deleteByUser(User user);

    /**
     * Delete cart by user ID
     */
    void deleteByUserId(UUID userId);

    /**
     * Check if user has a cart
     */
    boolean existsByUserId(UUID userId);

    /**
     * Find cart with items eagerly loaded
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);
}