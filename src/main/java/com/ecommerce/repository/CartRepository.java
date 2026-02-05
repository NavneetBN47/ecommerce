package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Cart entity
 * Supports lazy cart creation and auto-delete empty cart logic
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    // Find empty carts for cleanup
    @Query("SELECT c FROM Cart c WHERE c.totalItems = 0 OR SIZE(c.items) = 0")
    List<Cart> findEmptyCarts();

    // Find abandoned carts (empty and not updated for specified duration)
    @Query("SELECT c FROM Cart c WHERE (c.totalItems = 0 OR SIZE(c.items) = 0) AND c.updatedAt < :cutoffTime")
    List<Cart> findAbandonedEmptyCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Delete empty carts (for auto-cleanup)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.totalItems = 0 OR SIZE(c.items) = 0")
    int deleteEmptyCarts();

    // Delete cart by user ID (for logout cleanup)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    // Delete empty cart by user ID
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId AND (c.totalItems = 0 OR SIZE(c.items) = 0)")
    int deleteEmptyCartByUserId(@Param("userId") Long userId);
}