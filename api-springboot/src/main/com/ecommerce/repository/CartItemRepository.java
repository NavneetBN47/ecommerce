package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * CartItem Repository
 * Data access layer for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart ID and product ID
     * @param cartId the cart ID
     * @param productId the product ID
     * @return Optional containing cart item if found
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Count items in cart
     * @param cartId the cart ID
     * @return count of items
     */
    long countByCartId(Long cartId);

    /**
     * Delete all items in cart
     * @param cartId the cart ID
     */
    void deleteByCartId(Long cartId);
}