package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service
 * Business logic for user management operations
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Sign up a new user
     * @param request sign-up request
     * @return user response
     */
    public UserResponse signUp(SignUpRequest request) {
        logger.info("Attempting to sign up user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.error("Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);
        logger.info("User signed up successfully: {}", savedUser.getUsername());

        return mapToUserResponse(savedUser);
    }

    /**
     * Sign in a user
     * @param request sign-in request
     * @return user response
     */
    public UserResponse signIn(SignInRequest request) {
        logger.info("Attempting to sign in user: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> {
                logger.error("Invalid username: {}", request.getUsername());
                return new InvalidCredentialsException("Invalid username or password");
            });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.error("Invalid password for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        logger.info("User signed in successfully: {}", user.getUsername());
        return mapToUserResponse(user);
    }

    /**
     * Get user profile
     * @param userId user ID
     * @return user response
     */
    public UserResponse getProfile(Long userId) {
        logger.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found: {}", userId);
                return new ResourceNotFoundException("User not found");
            });

        return mapToUserResponse(user);
    }

    /**
     * Update user profile
     * @param userId user ID
     * @param request update profile request
     * @return user response
     */
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        logger.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found: {}", userId);
                return new ResourceNotFoundException("User not found");
            });

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", updatedUser.getUsername());

        return mapToUserResponse(updatedUser);
    }

    /**
     * Logout user and cleanup cart
     * @param userId user ID
     */
    public void logout(Long userId) {
        logger.info("Logging out user ID: {}", userId);
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            logger.error("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        // Delete cart on logout
        cartService.deleteCartByUserId(userId);
        logger.info("User logged out successfully, cart deleted for user ID: {}", userId);
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setCreatedDate(user.getCreatedAt());
        return response;
    }
}