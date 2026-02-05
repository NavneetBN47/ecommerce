package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Cart.CartStatus;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findByUserAndStatus(User user, CartStatus status);
    
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
}