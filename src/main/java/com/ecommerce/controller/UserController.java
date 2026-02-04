package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    
    @PostMapping("/signup")
    public ResponseEntity<UserProfileResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        log.info("Received signup request for username: {}", request.getUsername());
        UserProfileResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request for username: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received profile request for user ID: {}", userId);
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received profile update request for user ID: {}", userId);
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    private UUID extractUserIdFromToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwtToken);
        return UUID.fromString(userId);
    }
}