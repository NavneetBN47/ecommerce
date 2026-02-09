package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Cart Repository - Data access layer for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Find cart by user
     * @param user the user
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID with items eagerly loaded
     * @param userId the user ID
     * @return Optional containing the cart with items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

    /**
     * Delete cart by user
     * @param user the user
     */
    void deleteByUser(User user);

    /**
     * Check if cart exists for user
     * @param user the user
     * @return true if cart exists
     */
    boolean existsByUser(User user);
}