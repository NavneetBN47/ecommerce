package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.exception.UsernameExistsException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CartService cartService;
    
    /**
     * Register a new user
     */
    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        log.info("Processing signup for username: {}", request.getUsername());
        
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new UsernameExistsException("Username already exists");
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new InvalidInputException("Email already exists");
        }
        
        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .isActive(true)
                .build();
        
        user.setFullName(request.getFullName());
        
        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());
        
        return mapToUserResponse(user);
    }
    
    /**
     * Authenticate user and return JWT token
     */
    @Transactional
    public LoginResponse login(UserLoginRequest request) {
        log.info("Processing login for username: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return LoginResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Get user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        return mapToUserResponse(user);
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUserProfile(UUID userId, UserUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        user = userRepository.save(user);
        log.info("User profile updated successfully: {}", userId);
        
        return mapToUserResponse(user);
    }
    
    /**
     * Logout user and cleanup cart
     */
    @Transactional
    public void logout(UUID userId) {
        log.info("Processing logout for user ID: {}", userId);
        
        // Delete user's cart and all items
        cartService.deleteUserCart(userId);
        
        log.info("User logged out and cart deleted: {}", userId);
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}