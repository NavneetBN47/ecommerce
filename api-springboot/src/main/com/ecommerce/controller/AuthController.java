package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 * Implements stateless login and logout with cart cleanup
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Login endpoint - returns JWT token for stateless authentication
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Register endpoint - creates new user account
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for user: {}", request.getUsername());
        User user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    /**
     * Logout endpoint - clears cart and invalidates session
     * POST /api/auth/logout
     * Business Rule: All cart data must be cleaned up when user logs out
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Logout request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        authService.logout(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}