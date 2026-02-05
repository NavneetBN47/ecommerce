package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for Authentication operations
 * Implements stateless login and logout with cart cleanup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user (stateless)
     */
    @Transactional(readOnly = true)
    public User authenticateUser(String username, String password) {
        log.info("Authenticating user: {}", username);
        
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
            .orElse(null);
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed for user: {}", username);
            return null;
        }
        
        if (!user.getIsActive()) {
            log.warn("User account is inactive: {}", username);
            return null;
        }
        
        log.info("User authenticated successfully: {}", username);
        return user;
    }

    /**
     * Logout user and cleanup cart
     */
    @Transactional
    public void logoutUser(Long userId) {
        log.info("Logging out user: {}", userId);
        
        // Cleanup cart on logout
        cartService.cleanupCartOnLogout(userId);
        
        log.info("User logged out successfully: {}", userId);
    }
}