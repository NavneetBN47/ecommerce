package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for user management operations
 * Implements LLD user management business logic
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * User sign-up (registration)
     * Implements LLD POST /api/users/signup
     */
    public UserResponse signUp(SignUpRequest request) {
        logger.info("Processing sign-up request for username: {}", request.getUsername());

        // Validate username uniqueness per LLD
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        // Parse full name into first and last name
        String[] nameParts = request.getFullName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(request.getEmail());
        user.setIsActive(true);
        user.setEmailVerified(false);

        user = userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .token(token)
                .build();
    }

    /**
     * User login
     * Implements LLD POST /api/users/login (stateless at DB level)
     */
    public UserResponse login(LoginRequest request) {
        logger.info("Processing login request for username: {}", request.getUsername());

        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        logger.info("User logged in successfully: {}", user.getUsername());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .token(token)
                .build();
    }

    /**
     * Get user profile
     * Implements LLD GET /api/users/profile
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        logger.info("Fetching profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Update user profile
     * Implements LLD PUT /api/users/profile
     */
    public UserResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        logger.info("Updating profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Parse full name into first and last name
        String[] nameParts = request.getFullName().trim().split("\\s+", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        // Check if email is being changed and validate uniqueness
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // Reset email verification
        }

        user = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", user.getUsername());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Get user by ID (internal use)
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}