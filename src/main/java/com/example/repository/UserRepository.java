package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email.
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);
    
    /**
     * Find active users by role.
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    Optional<User> findActiveUsersByRole(@Param("role") User.UserRole role);
}