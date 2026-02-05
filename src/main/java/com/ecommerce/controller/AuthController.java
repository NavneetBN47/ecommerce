package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication operations
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login request received for: {}", authRequest.getUsernameOrEmail());
        AuthResponse authResponse = authService.login(authRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserDTO userDTO) {
        log.info("Registration request received for: {}", userDTO.getUsername());
        AuthResponse authResponse = authService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Registration successful", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and cleanup cart if configured")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser Long userId) {
        log.info("Logout request received for user ID: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}