package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 * Provides database operations for order item management
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all items in an order
     */
    List<OrderItem> findByOrder_OrderId(Long orderId);
}