package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, Cart.CartStatus status);

    List<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId AND c.status = :status")
    Optional<Cart> findActiveCartByUserIdWithItems(@Param("userId") Long userId, @Param("status") Cart.CartStatus status);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    void deleteActiveCartsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id IN (SELECT cart.id FROM Cart cart WHERE cart.user.id = :userId AND SIZE(cart.items) = 0)")
    void deleteEmptyCartsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    long countActiveCartsByUserId(@Param("userId") Long userId);
}