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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
        description = "Creates a new user account with email, password, and profile information. Passwords are hashed using BCrypt with cost factor 12."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or user already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<UserRegistrationResponse> registerUser(
        @Valid @RequestBody @Parameter(description = "User registration details", required = true) UserRegistrationRequest request
    ) {
        UserRegistrationResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password, returns JWT token for subsequent API calls"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful, JWT token returned",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<UserLoginResponse> loginUser(
        @Valid @RequestBody @Parameter(description = "User login credentials", required = true) UserLoginRequest request
    ) {
        UserLoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
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
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<UserProfileResponse> getUserProfile(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails
    ) {
        UserProfileResponse response = userService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
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
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<UserProfileResponse> updateUserProfile(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails,
        @Valid @RequestBody @Parameter(description = "Updated profile information", required = true) UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateUserProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/change")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Change password",
        description = "Allows authenticated user to change their password by providing current and new password"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid password or password requirements not met",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<MessageResponse> changePassword(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails,
        @Valid @RequestBody @Parameter(description = "Current and new password", required = true) PasswordChangeRequest request
    ) {
        MessageResponse response = userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/reset")
    @Operation(
        summary = "Request password reset",
        description = "Initiates password reset process by sending reset link to user's email"
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
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Email service error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<MessageResponse> resetPassword(
        @Valid @RequestBody @Parameter(description = "Email address for password reset", required = true) PasswordResetRequest request
    ) {
        MessageResponse response = userService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Validate user existence",
        description = "Internal API to validate if a user exists (used by other microservices)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User validation result",
            content = @Content(schema = @Schema(implementation = UserValidationResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<UserValidationResponse> validateUser(
        @PathVariable @Parameter(description = "User ID to validate", required = true) Long userId
    ) {
        UserValidationResponse response = userService.validateUser(userId);
        return ResponseEntity.ok(response);
    }
}

class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String timestamp;
}

class UserValidationResponse {
    private boolean exists;
    private Long userId;
}