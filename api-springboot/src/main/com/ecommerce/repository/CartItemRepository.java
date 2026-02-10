package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity
 * Provides database operations for cart item management
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all items in a cart
     */
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);

    /**
     * Find cart item by cart ID and product ID
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    /**
     * Delete all items in a cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Count items in a cart
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    long countByCartId(@Param("cartId") Long cartId);

    /**
     * Check if cart has items
     */
    boolean existsByCart_CartId(Long cartId);
}