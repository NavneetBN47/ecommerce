package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all items in an order
     */
    List<OrderItem> findByOrder(Order order);
    
    /**
     * Find all items in an order by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Count items in an order
     */
    long countByOrderId(Long orderId);
}