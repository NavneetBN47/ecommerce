package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * Delete cart by user
     */
    void deleteByUser(User user);
    
    /**
     * Delete cart by user ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * Delete empty carts (carts with no items)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id NOT IN (SELECT DISTINCT ci.cart.id FROM CartItem ci)")
    void deleteEmptyCarts();
    
    /**
     * Check if cart exists for user
     */
    boolean existsByUserId(Long userId);
}