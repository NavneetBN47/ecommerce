package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Order entity
 * Provides database operations for order management
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a user
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find orders by status
     */
    List<Order> findByOrderStatus(String orderStatus);

    /**
     * Find orders by user and status
     */
    List<Order> findByUserIdAndOrderStatus(Long userId, String orderStatus);

    /**
     * Find order with items
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderId = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);
}