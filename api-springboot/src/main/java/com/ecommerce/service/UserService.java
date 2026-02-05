package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for User operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CartService cartService;

    /**
     * Register a new user
     */
    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering new user: {}", registrationDTO.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + registrationDTO.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + registrationDTO.getEmail());
        }

        // Create new user
        User user = User.builder()
            .username(registrationDTO.getUsername())
            .email(registrationDTO.getEmail())
            .passwordHash(passwordEncoder.encode(registrationDTO.getPassword()))
            .fullName(registrationDTO.getFullName())
            .phone(registrationDTO.getPhone())
            .address(registrationDTO.getAddress())
            .isActive(true)
            .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUserId());

        return mapToResponseDTO(user);
    }

    /**
     * Authenticate user and generate JWT token (stateless)
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO login(UserLoginDTO loginDTO) {
        log.info("User login attempt: {}", loginDTO.getUsernameOrEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsernameOrEmail(),
                loginDTO.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);
        Long expiresIn = jwtTokenProvider.getJwtExpirationInMs();

        // Get user details
        User user = userRepository.findByUsernameOrEmail(loginDTO.getUsernameOrEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in successfully: {}", user.getUserId());

        return AuthResponseDTO.of(token, expiresIn, mapToResponseDTO(user));
    }

    /**
     * Logout user - cleanup cart if empty
     */
    @Transactional
    public void logout(Long userId) {
        log.info("User logout: {}", userId);
        
        // Clean up empty cart on logout
        cartService.cleanupEmptyCart(userId);
        
        // Clear security context
        SecurityContextHolder.clearContext();
        
        log.info("User logged out successfully: {}", userId);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToResponseDTO(user);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Map User entity to UserResponseDTO
     */
    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .address(user.getAddress())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}