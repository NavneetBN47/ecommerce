package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<UserDTO>> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Registration request received for username: {}", registrationDTO.getUsername());
        UserDTO user = userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDTO.success("User registered successfully", user));
    }

    /**
     * Login user (stateless with JWT)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login request received for: {}", loginRequest.getIdentifier());
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponseDTO.success("Login successful", response));
    }

    /**
     * Logout user (cleanup cart)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Logout request received for user: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Logout successful", null));
    }
}