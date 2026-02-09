package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CartItem Repository - Data access layer for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find cart item by cart and product
     * @param cart the cart
     * @param product the product
     * @return Optional containing the cart item if found
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Find all items in a cart
     * @param cart the cart
     * @return list of cart items
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find cart item by ID with cart and product eagerly loaded
     * @param id the cart item ID
     * @return Optional containing the cart item
     */
    @Query("SELECT ci FROM CartItem ci " +
           "LEFT JOIN FETCH ci.cart c " +
           "LEFT JOIN FETCH ci.product p " +
           "WHERE ci.id = :id")
    Optional<CartItem> findByIdWithCartAndProduct(@Param("id") UUID id);

    /**
     * Count items in a cart
     * @param cart the cart
     * @return number of items
     */
    long countByCart(Cart cart);

    /**
     * Delete all items in a cart
     * @param cart the cart
     */
    void deleteByCart(Cart cart);
}