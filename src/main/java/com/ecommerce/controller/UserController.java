package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final CartService cartService;
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("POST /api/users/register - username: {}", request.getUsername());
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("POST /api/users/login - username: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestAttribute("userId") Long userId) {
        log.info("POST /api/users/logout - userId: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestAttribute("userId") Long userId) {
        log.info("GET /api/users/profile - userId: {}", userId);
        UserResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("PATCH /api/users/profile - userId: {}", userId);
        UserResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
