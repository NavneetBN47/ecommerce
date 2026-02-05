package com.example.repository;

import com.example.entity.Cart;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity operations.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Find cart by user.
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * Find cart by user ID.
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    /**
     * Delete cart by user.
     */
    void deleteByUser(User user);
    
    /**
     * Delete empty carts (carts with no items).
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.totalItems = 0 OR c.totalItems IS NULL")
    void deleteEmptyCarts();
    
    /**
     * Check if user has a cart.
     */
    boolean existsByUser(User user);
    
    /**
     * Check if user has a cart by user ID.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}