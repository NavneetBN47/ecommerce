package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.updatedAt < :cutoffDate")
    void deleteAbandonedCarts(@Param("cutoffDate") LocalDateTime cutoffDate);
}