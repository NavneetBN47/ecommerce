package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    /**
     * Find all items in a cart
     * @param cartId cart UUID
     * @return list of cart items
     */
    List<CartItem> findByCartId(UUID cartId);
    
    /**
     * Find cart item by cart and product
     * @param cartId cart UUID
     * @param productId product UUID
     * @return Optional containing cart item if exists
     */
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
    
    /**
     * Count items in cart
     * @param cartId cart UUID
     * @return number of items
     */
    long countByCartId(UUID cartId);
    
    /**
     * Delete all items for a cart
     * @param cartId cart UUID
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") UUID cartId);
    
    /**
     * Find cart item by ID and cart ID (for security)
     * @param id cart item UUID
     * @param cartId cart UUID
     * @return Optional containing cart item
     */
    Optional<CartItem> findByIdAndCartId(UUID id, UUID cartId);
}