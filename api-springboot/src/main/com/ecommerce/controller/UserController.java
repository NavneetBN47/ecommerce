package com.ecommerce.controller;

import com.ecommerce.dto.LoginRequestDTO;
import com.ecommerce.dto.LoginResponseDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * Handles user authentication and profile management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user registration, login, logout, and profile management")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "User Registration", description = "Register a new user (no cart created at signup)")
    public ResponseEntity<UserDTO> signUp(@Valid @RequestBody UserDTO userDTO) {
        log.info("POST /api/users/signup - User registration request");
        UserDTO registeredUser = userService.signUp(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/signin")
    @Operation(summary = "User Login", description = "Authenticate user with username and password")
    public ResponseEntity<LoginResponseDTO> signIn(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("POST /api/users/signin - User login request");
        LoginResponseDTO response = userService.signIn(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout/{userId}")
    @Operation(summary = "User Logout", description = "Logout user and clear cart")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        log.info("POST /api/users/logout/{} - User logout request", userId);
        userService.logout(userId);
        return ResponseEntity.ok("Logout successful. Cart cleared.");
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get User Profile", description = "Retrieve user profile information")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long userId) {
        log.info("GET /api/users/{} - Fetch user profile", userId);
        UserDTO userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update User Profile", description = "Update user profile information")
    public ResponseEntity<UserDTO> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("PUT /api/users/{} - Update user profile", userId);
        UserDTO updatedUser = userService.updateUserProfile(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }
}