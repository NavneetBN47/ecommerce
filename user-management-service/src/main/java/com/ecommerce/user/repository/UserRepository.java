package com.ecommerce.user.repository;

import com.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isActive = true")
    Optional<User> findActiveUserByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.isActive = true")
    Optional<User> findActiveUserByUsername(String username);
}