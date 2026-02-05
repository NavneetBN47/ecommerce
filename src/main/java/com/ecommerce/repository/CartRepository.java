package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Cart Repository with support for lazy creation and auto-cleanup
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserAndStatus(User user, Cart.CartStatus status);

    Optional<Cart> findByUserIdAndStatus(Long userId, Cart.CartStatus status);

    List<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Long userId);

    /**
     * Delete empty carts (carts with no items)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id IN " +
           "(SELECT c2.id FROM Cart c2 WHERE SIZE(c2.items) = 0)")
    void deleteEmptyCarts();

    /**
     * Delete user's empty carts on logout
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId AND SIZE(c.items) = 0")
    void deleteEmptyCartsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItems(@Param("cartId") Long cartId);
}