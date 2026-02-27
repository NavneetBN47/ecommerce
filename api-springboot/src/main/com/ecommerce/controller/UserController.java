package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * REST API endpoints for user management
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Sign up a new user
     * POST /api/users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        logger.info("POST /api/users/signup - Username: {}", request.getUsername());
        UserResponse response = userService.signUp(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Sign in a user
     * POST /api/users/signin
     */
    @PostMapping("/signin")
    public ResponseEntity<UserResponse> signIn(@Valid @RequestBody SignInRequest request) {
        logger.info("POST /api/users/signin - Username: {}", request.getUsername());
        UserResponse response = userService.signIn(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user profile
     * GET /api/users/me
     * Note: In production, userId should come from JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("X-User-Id") Long userId) {
        logger.info("GET /api/users/me - User ID: {}", userId);
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile
     * PUT /api/users/me
     * Note: In production, userId should come from JWT token
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        logger.info("PUT /api/users/me - User ID: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user and cleanup cart
     * POST /api/users/logout
     * Note: In production, userId should come from JWT token
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-User-Id") Long userId) {
        logger.info("POST /api/users/logout - User ID: {}", userId);
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }
}