package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find all orders by user
     */
    List<Order> findByUser(User user);
    
    /**
     * Find all orders by user ID
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find orders by user and status
     */
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    /**
     * Find orders by user ID and status
     */
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    
    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);
}