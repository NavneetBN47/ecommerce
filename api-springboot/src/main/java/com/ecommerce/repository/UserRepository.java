package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 * Provides database access for user operations per LLD
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username (unique identifier per LLD)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists (for uniqueness validation per LLD)
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}