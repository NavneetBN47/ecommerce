package com.ecommerce.service;

import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
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
 * Service layer for Authentication operations
 * Implements stateless login with JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CartService cartService;

    /**
     * Stateless login - returns JWT token
     */
    @Transactional
    public AuthResponse login(AuthRequest authRequest) {
        log.info("Login attempt for user: {}", authRequest.getUsernameOrEmail());
        
        User user = userRepository.findByUsernameOrEmail(
            authRequest.getUsernameOrEmail(), 
            authRequest.getUsernameOrEmail())
            .orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));
        
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {}", authRequest.getUsernameOrEmail());
            throw new AuthenticationException("Invalid username/email or password");
        }
        
        if (!user.getActive()) {
            log.warn("Login attempt for inactive user: {}", authRequest.getUsernameOrEmail());
            throw new AuthenticationException("User account is inactive");
        }
        
        // Update last login
        userService.updateLastLogin(user.getId());
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        UserDTO userDTO = UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .build();
        
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .user(userDTO)
            .message("Login successful")
            .build();
    }

    /**
     * Logout with cart cleanup
     */
    @Transactional
    public void logout(Long userId) {
        log.info("Logout for user ID: {}", userId);
        
        // Cleanup cart if configured
        cartService.cleanupCartOnLogout(userId);
        
        log.info("User logged out successfully: {}", userId);
    }

    @Transactional
    public AuthResponse register(UserDTO userDTO) {
        log.info("Registration attempt for username: {}", userDTO.getUsername());
        
        // Create user
        UserDTO createdUser = userService.createUser(userDTO);
        
        log.info("User registered successfully: {}", createdUser.getUsername());
        
        return AuthResponse.builder()
            .user(createdUser)
            .message("Registration successful. Please login.")
            .build();
    }
}