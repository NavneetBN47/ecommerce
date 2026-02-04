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
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("POST /api/users/signup - Sign up request for username: {}", request.getUsername());
        UserResponse response = userService.signUp(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/users/login - Login request for username: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestAttribute("userId") UUID userId) {
        log.info("GET /api/users/profile - Get profile for user ID: {}", userId);
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/users/profile - Update profile for user ID: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}