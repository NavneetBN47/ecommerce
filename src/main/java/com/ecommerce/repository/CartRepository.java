package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    Optional<Cart> findByUser(User user);
    
    Optional<Cart> findByUserId(UUID userId);
    
    void deleteByUser(User user);
    
    void deleteByUserId(UUID userId);
}