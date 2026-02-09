package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity
 * Provides database operations for user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email address
     * @param email user's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email already exists
     * @param email email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find active users by email
     * @param email user's email
     * @return Optional containing active user
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);
}