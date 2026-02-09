package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatus(@Param("userId") Long userId, 
                                       @Param("status") Order.OrderStatus status, 
                                       Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderId = :orderId AND o.user.userId = :userId")
    Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}