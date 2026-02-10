package com.ecommerce.service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication
 * Implements stateless login with JWT tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CartService cartService;

    /**
     * Login user and return JWT token
     * Business Rule: Stateless authentication using JWT
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userService.findActiveUserByUsername(request.getUsername());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            log.info("Login successful for user: {}", request.getUsername());

            return new LoginResponse(
                    token,
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail()
            );

        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    /**
     * Logout user and cleanup cart
     * Business Rule: All cart data must be cleaned up when user logs out
     */
    @Transactional
    public void logout(Long userId) {
        log.info("Logging out user: {}", userId);
        
        // Clear cart on logout
        cartService.clearCart(userId);
        
        log.info("Logout successful for user: {}", userId);
    }
}