package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.userId = :userId")
    Optional<Cart> findByUserUserId(@Param("userId") Long userId);

    /**
     * Find cart by user ID and status
     */
    Optional<Cart> findByUserUserIdAndStatus(Long userId, String status);

    /**
     * Delete cart by user ID
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.userId = :userId")
    void deleteByUserUserId(@Param("userId") Long userId);

    /**
     * Check if user has active cart
     */
    boolean existsByUserUserIdAndStatus(Long userId, String status);
}