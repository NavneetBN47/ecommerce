package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for user management operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * POST /api/users/signup - Register new user
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        log.info("Signup request received for username: {}", request.getUsername());
        UserResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/users/login - Authenticate user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/users/me - Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UUID userId) {
        log.info("Get profile request for user: {}", userId);
        UserResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/users/me - Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @CurrentUser UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Update profile request for user: {}", userId);
        UserResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/logout - Logout user and cleanup cart
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@CurrentUser UUID userId) {
        log.info("Logout request for user: {}", userId);
        userService.logout(userId);
        return ResponseEntity.ok(Map.of("message", "Logged out. Cart deleted."));
    }
}