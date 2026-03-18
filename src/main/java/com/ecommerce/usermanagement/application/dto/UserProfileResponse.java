package com.ecommerce.usermanagement.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response")
public class UserProfileResponse {
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "First name", example = "John")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Address", example = "123 Main St, City, State 12345")
    private String address;
    
    @Schema(description = "User role", example = "CUSTOMER")
    private String role;
    
    @Schema(description = "Account status", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last login timestamp", example = "2024-01-15T14:20:00")
    private LocalDateTime lastLoginAt;
}