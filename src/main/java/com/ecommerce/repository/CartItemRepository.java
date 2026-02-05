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
     * Find all items in a cart
     */
    List<CartItem> findByCart(Cart cart);
    
    /**
     * Find all items in a cart by cart ID
     */
    List<CartItem> findByCartId(Long cartId);
    
    /**
     * Find cart item by cart and product
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    /**
     * Find cart item by cart ID and product ID
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    /**
     * Delete all items in a cart
     */
    void deleteByCart(Cart cart);
    
    /**
     * Delete all items in a cart by cart ID
     */
    void deleteByCartId(Long cartId);
    
    /**
     * Count items in a cart
     */
    long countByCartId(Long cartId);
}