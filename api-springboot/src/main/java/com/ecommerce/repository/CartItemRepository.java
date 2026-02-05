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
 * Repository for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart ID and product ID
     */
    Optional<CartItem> findByCartCartIdAndProductProductId(Long cartId, Long productId);

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCartCartId(Long cartId);

    /**
     * Delete all items in a cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartCartId(@Param("cartId") Long cartId);

    /**
     * Count items in cart
     */
    long countByCartCartId(Long cartId);
}