package com.example.repository;

import com.example.entity.Order;
import com.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find orders by user.
     */
    Page<Order> findByUser(User user, Pageable pageable);
    
    /**
     * Find orders by user ID.
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find orders by status.
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC")
    Page<Order> findByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by user and status.
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.orderDate DESC")
    Page<Order> findByUserIdAndStatus(@Param("userId") Long userId, 
                                       @Param("status") Order.OrderStatus status, 
                                       Pageable pageable);
    
    /**
     * Find orders within date range.
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Check if order number exists.
     */
    boolean existsByOrderNumber(String orderNumber);
}