package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    Optional<Cart> findByUserIdAndCartStatus(UUID userId, Cart.CartStatus cartStatus);
    
    List<Cart> findByUserId(UUID userId);
    
    List<Cart> findByCartStatus(Cart.CartStatus cartStatus);
    
    @Query("SELECT c FROM Cart c WHERE c.expiresAt < :currentTime AND c.cartStatus = 'ACTIVE'")
    List<Cart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.cartId = :cartId")
    Optional<Cart> findByIdWithItems(@Param("cartId") UUID cartId);
}