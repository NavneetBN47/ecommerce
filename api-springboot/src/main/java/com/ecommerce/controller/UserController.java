package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for user management operations
 * Implements LLD user management API contracts
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for user registration, login, and profile management")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * User sign-up endpoint
     * Implements LLD POST /api/users/signup
     */
    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        logger.info("Sign-up request received for username: {}", request.getUsername());
        UserResponse response = userService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * User login endpoint
     * Implements LLD POST /api/users/login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for username: {}", request.getUsername());
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Get user profile endpoint
     * Implements LLD GET /api/users/profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Retrieve authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Profile request received for user ID: {}", userId);
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user profile endpoint
     * Implements LLD PUT /api/users/profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update authenticated user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Profile update request received for user ID: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}