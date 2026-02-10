package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getCurrentUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Get current user request for: {}", userId);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(user));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateCurrentUser(
            @Valid @RequestBody UserDTO userDTO,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Update user request for: {}", userId);
        UserDTO updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("User updated successfully", updatedUser));
    }

    /**
     * Deactivate current user account
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponseDTO<Void>> deactivateCurrentUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Deactivate user request for: {}", userId);
        userService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("User deactivated successfully", null));
    }
}