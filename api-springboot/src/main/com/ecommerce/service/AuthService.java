package com.ecommerce.service;

import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.AuthenticationException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service - Handles authentication and logout cleanup
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user (stateless login)
     */
    public AuthResponse login(AuthRequest authRequest) {
        log.info("Authenticating user: {}", authRequest.getUsername());

        User user = userRepository.findByUsernameOrEmail(authRequest.getUsername())
            .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        if (!user.getIsActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        log.info("User authenticated successfully: {}", user.getUsername());

        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .message("Login successful")
            .build();
    }

    /**
     * Logout user with cart cleanup
     */
    public void logout(Long userId) {
        log.info("Logging out user ID: {}", userId);

        // Clear user's cart on logout
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            log.info("Clearing cart for user ID: {} on logout", userId);
            cartRepository.delete(cart);
        });

        log.info("User logged out successfully: {}", userId);
    }
}