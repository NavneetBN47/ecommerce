package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Controller
 * Handles user management endpoints
 * API Contracts as per LLD Section 4.1
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * POST /api/users/signup
     * Register a new user
     * @param registrationDTO user registration data
     * @return 201 Created with user response
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Signup request for username: {}", registrationDTO.getUsername());
        UserResponseDTO response = userService.registerUser(registrationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/users/login
     * Authenticate user
     * @param loginDTO user credentials
     * @return 200 OK with user response and token
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("Login request for username: {}", loginDTO.getUsername());
        UserResponseDTO response = userService.loginUser(loginDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/me
     * Get current user profile
     * @param authentication current authenticated user
     * @return 200 OK with user response
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Get profile request for user: {}", userId);
        UserResponseDTO response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/me
     * Update current user profile
     * @param updateDTO user update data
     * @param authentication current authenticated user
     * @return 200 OK with updated user response
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @Valid @RequestBody UserUpdateDTO updateDTO,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Update profile request for user: {}", userId);
        UserResponseDTO response = userService.updateUserProfile(userId, updateDTO);
        return ResponseEntity.ok(response);
    }
}