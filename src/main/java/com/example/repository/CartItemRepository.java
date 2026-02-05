package com.example.repository;

import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for CartItem entity operations.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Find cart item by cart and product.
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    /**
     * Delete cart items by cart.
     */
    void deleteByCart(Cart cart);
    
    /**
     * Delete cart item by cart and product.
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product")
    void deleteByCartAndProduct(@Param("cart") Cart cart, @Param("product") Product product);
    
    /**
     * Count items in cart.
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart = :cart")
    Long countByCart(@Param("cart") Cart cart);
}