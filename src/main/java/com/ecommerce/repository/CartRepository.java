package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
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
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find active cart for user
     */
    Optional<Cart> findByUserAndStatus(User user, Cart.CartStatus status);

    /**
     * Find active cart by user ID
     */
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.status = :status")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Long userId, @Param("status") Cart.CartStatus status);

    /**
     * Find all carts for a user
     */
    List<Cart> findByUser(User user);

    /**
     * Delete empty carts (auto-cleanup)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.totalItems = 0 OR c.totalItems IS NULL")
    void deleteEmptyCarts();

    /**
     * Delete abandoned carts older than specified date
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.status = 'ABANDONED' AND c.updatedAt < :cutoffDate")
    void deleteAbandonedCartsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find carts by status
     */
    List<Cart> findByStatus(Cart.CartStatus status);

    /**
     * Count active carts for user
     */
    long countByUserAndStatus(User user, Cart.CartStatus status);
}