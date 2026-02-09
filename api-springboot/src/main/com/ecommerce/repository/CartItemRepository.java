package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cartItemId = :cartItemId AND ci.cart.user.userId = :userId")
    Optional<CartItem> findByIdAndUserId(@Param("cartItemId") Long cartItemId, @Param("userId") Long userId);
}