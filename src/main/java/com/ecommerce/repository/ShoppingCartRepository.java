package com.ecommerce.repository;

import com.ecommerce.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ShoppingCart entity
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @Query("SELECT c FROM ShoppingCart c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<ShoppingCart> findActiveCartByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
    Optional<ShoppingCart> findByIdWithItems(@Param("cartId") Long cartId);

    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product " +
           "WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<ShoppingCart> findActiveCartByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT c FROM ShoppingCart c WHERE c.status = 'ACTIVE' " +
           "AND c.updatedAt < :thresholdDate")
    List<ShoppingCart> findInactiveCarts(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Modifying
    @Query("UPDATE ShoppingCart c SET c.status = 'ABANDONED' " +
           "WHERE c.status = 'ACTIVE' AND c.updatedAt < :thresholdDate")
    int markInactiveCartsAsAbandoned(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Modifying
    @Query("DELETE FROM ShoppingCart c WHERE c.status = 'ABANDONED' " +
           "AND c.updatedAt < :thresholdDate")
    int deleteAbandonedCarts(@Param("thresholdDate") LocalDateTime thresholdDate);

    List<ShoppingCart> findByUserId(Long userId);
}