package com.ecommerce.usermanagement.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile update request")
public class UserProfileUpdateRequest {
    
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Schema(description = "First name", example = "John")
    private String firstName;
    
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Address", example = "123 Main St, City, State 12345")
    private String address;
}