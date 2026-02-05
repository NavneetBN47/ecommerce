package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CartItem entity
 * Provides database access methods for cart item operations
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find cart item by cart ID and product ID
     * @param cartId the cart ID
     * @param productId the product ID
     * @return Optional containing cart item if found
     */
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    /**
     * Find all cart items by cart ID
     * @param cartId the cart ID
     * @return List of cart items
     */
    List<CartItem> findByCartId(UUID cartId);

    /**
     * Delete all cart items by cart ID
     * @param cartId the cart ID
     */
    void deleteByCartId(UUID cartId);

    /**
     * Count cart items by cart ID
     * @param cartId the cart ID
     * @return count of items
     */
    long countByCartId(UUID cartId);
}