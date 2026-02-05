package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart and product
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find all items for a specific product
     */
    List<CartItem> findByProduct(Product product);

    /**
     * Delete all items in a cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteByCart(@Param("cart") Cart cart);

    /**
     * Count items in a cart
     */
    long countByCart(Cart cart);

    /**
     * Check if product exists in cart
     */
    boolean existsByCartAndProduct(Cart cart, Product product);
}