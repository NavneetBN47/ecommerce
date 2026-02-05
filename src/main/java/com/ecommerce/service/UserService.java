package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ErrorCode;
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
 * Implements business logic for user registration, authentication, and profile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     * Business Rule: Username must be unique
     * @param registrationDTO user registration data
     * @return user response with token
     */
    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering new user: {}", registrationDTO.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            log.error("Username already exists: {}", registrationDTO.getUsername());
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, 
                "Username '" + registrationDTO.getUsername() + "' is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            log.error("Email already exists: {}", registrationDTO.getEmail());
            throw new BusinessException(ErrorCode.INVALID_INPUT, 
                "Email '" + registrationDTO.getEmail() + "' is already registered");
        }

        // Parse full name into first and last name
        String[] nameParts = registrationDTO.getFullName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create new user
        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(registrationDTO.getEmail());
        user.setIsActive(true);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUserId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUserId().toString());

        return UserResponseDTO.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .token(token)
                .build();
    }

    /**
     * Authenticate user and generate token
     * Business Rule: Stateless authentication
     * @param loginDTO user login credentials
     * @return user response with token
     */
    @Transactional
    public UserResponseDTO loginUser(UserLoginDTO loginDTO) {
        log.info("User login attempt: {}", loginDTO.getUsername());

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> {
                    log.error("Invalid credentials for username: {}", loginDTO.getUsername());
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS, 
                        "Invalid username or password");
                });

        // Verify password
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            log.error("Invalid password for username: {}", loginDTO.getUsername());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid username or password");
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUserId().toString());

        log.info("User logged in successfully: {}", user.getUserId());

        return UserResponseDTO.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .token(token)
                .build();
    }

    /**
     * Get user profile
     * @param userId the user ID
     * @return user response
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfile(UUID userId) {
        log.info("Fetching user profile: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new BusinessException(ErrorCode.UNAUTHORIZED, "User not found");
                });

        return UserResponseDTO.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Update user profile
     * @param userId the user ID
     * @param updateDTO user update data
     * @return updated user response
     */
    @Transactional
    public UserResponseDTO updateUserProfile(UUID userId, UserUpdateDTO updateDTO) {
        log.info("Updating user profile: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new BusinessException(ErrorCode.UNAUTHORIZED, "User not found");
                });

        // Parse full name
        String[] nameParts = updateDTO.getFullName().trim().split("\\s+", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setEmail(updateDTO.getEmail());

        user = userRepository.save(user);
        log.info("User profile updated successfully: {}", userId);

        return UserResponseDTO.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}