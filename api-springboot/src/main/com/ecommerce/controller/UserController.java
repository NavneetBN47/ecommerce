package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("POST /api/users/signup - Registering new user: {}", request.getUsername());
        UserResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/users/login - User login attempt: {}", request.getUsername());
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("X-User-Id") UUID userId) {
        log.info("GET /api/users/profile - Fetching profile for user: {}", userId);
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/users/profile - Updating profile for user: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}