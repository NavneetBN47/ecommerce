package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity operations
 * Supports lazy creation and auto-cleanup functionality
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    /**
     * Find cart by user ID with items and products
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Delete cart by user ID (for logout cleanup)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Delete empty carts (auto-cleanup)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.totalItems = 0 OR c.items IS EMPTY")
    int deleteEmptyCarts();

    /**
     * Check if user has a cart
     */
    boolean existsByUserId(Long userId);
}