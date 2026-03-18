package com.ecommerce.usermanagement.presentation.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with email verification. Passwords are hashed using BCrypt with cost factor 12."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or user already exists",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<UserRegistrationResponse> registerUser(
        @Parameter(description = "User registration details", required = true)
        @Valid @RequestBody UserRegistrationRequest request
    ) {
        UserRegistrationResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user credentials and returns JWT access and refresh tokens (RSA-256 signed)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Account locked or disabled",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<UserLoginResponse> loginUser(
        @Parameter(description = "User login credentials", required = true)
        @Valid @RequestBody UserLoginRequest request
    ) {
        UserLoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get user profile",
        description = "Retrieves the authenticated user's profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<UserProfileResponse> getUserProfile(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId
    ) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update user profile",
        description = "Updates the authenticated user's profile information (name, phone, address)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<UserProfileResponse> updateUserProfile(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId,
        @Parameter(description = "Updated profile information", required = true)
        @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Change password",
        description = "Allows authenticated users to change their password by providing current and new password"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid password format or current password incorrect",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<MessageResponse> changePassword(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId,
        @Parameter(description = "Current and new password", required = true)
        @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @PostMapping("/password/reset")
    @Operation(
        summary = "Request password reset",
        description = "Initiates password reset process by sending a reset token to the user's email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many reset requests",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<MessageResponse> requestPasswordReset(
        @Parameter(description = "Email address for password reset", required = true)
        @Valid @RequestBody PasswordResetRequest request
    ) {
        userService.requestPasswordReset(request);
        return ResponseEntity.ok(new MessageResponse("Password reset email sent"));
    }

    @DeleteMapping("/account")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Delete user account",
        description = "Permanently deletes the authenticated user's account and all associated data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Account deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        )
    })
    public ResponseEntity<Void> deleteAccount(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId
    ) {
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}