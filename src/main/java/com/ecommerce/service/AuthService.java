package com.ecommerce.service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.AuthenticationException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Authentication operations
 * Implements stateless login with JWT tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CartService cartService;
    
    /**
     * Register a new user
     */
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        return userService.registerUser(request);
    }
    
    /**
     * Login user (stateless)
     */
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getIdentifier());
        
        User user = userRepository.findByUsernameOrEmail(request.getIdentifier())
            .orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {}", request.getIdentifier());
            throw new AuthenticationException("Invalid username/email or password");
        }
        
        if (!user.getActive()) {
            log.warn("Login attempt for inactive user: {}", request.getIdentifier());
            throw new AuthenticationException("User account is inactive");
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        UserDTO userDTO = UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .active(user.getActive())
            .build();
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .user(userDTO)
            .build();
    }
    
    /**
     * Logout user and cleanup empty cart
     */
    public void logout(Long userId) {
        log.info("User logout: {}", userId);
        
        // Cleanup empty cart on logout
        cartService.cleanupCartOnLogout(userId);
        
        log.info("User logged out successfully: {}", userId);
    }
}