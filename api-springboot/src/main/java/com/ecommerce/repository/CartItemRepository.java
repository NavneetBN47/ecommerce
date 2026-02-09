package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CartItem entity
 * Provides database access for cart item operations per LLD
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find cart item by cart and product (for duplicate check)
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Find cart item by ID and cart user ID (for authorization)
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :itemId AND ci.cart.user.id = :userId")
    Optional<CartItem> findByIdAndUserId(@Param("itemId") UUID itemId, @Param("userId") UUID userId);

    /**
     * Delete all cart items for a user (for logout cleanup per LLD)
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Count items in a cart
     */
    long countByCartId(UUID cartId);
}