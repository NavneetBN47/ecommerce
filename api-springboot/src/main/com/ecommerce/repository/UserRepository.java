package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * User Repository
 * Data access layer for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * @param username the username
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username exists
     * @param username the username
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Find user by username and password (for authentication)
     * @param username the username
     * @param password the hashed password
     * @return Optional containing user if credentials match
     */
    Optional<User> findByUsernameAndPassword(String username, String password);
}