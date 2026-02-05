package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    Optional<Cart> findByUserIdAndStatus(UUID userId, String status);
    
    Optional<Cart> findByUserId(UUID userId);
    
    void deleteByUserId(UUID userId);
}