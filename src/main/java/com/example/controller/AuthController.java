package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication operations
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout user and cleanup cart")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /api/v1/auth/logout - userId: {}", userId);
        
        authService.logoutUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}