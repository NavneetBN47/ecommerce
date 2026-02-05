package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserResponseDTO user = userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully", user));
    }

    /**
     * Login user (stateless - returns JWT token)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody UserLoginDTO loginDTO) {
        AuthResponseDTO authResponse = userService.login(loginDTO);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Logout user (cleanup empty cart)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser UserPrincipal currentUser) {
        userService.logout(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(
            @CurrentUser UserPrincipal currentUser) {
        UserResponseDTO user = userService.getUserById(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}