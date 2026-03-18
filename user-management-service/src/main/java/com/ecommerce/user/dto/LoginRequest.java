package com.ecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public class LoginRequest {
    
    @Schema(description = "Username or email", example = "john_doe", required = true)
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
    
    @Schema(description = "Password", example = "SecurePass123!", required = true)
    @NotBlank(message = "Password is required")
    private String password;
    
    public String getUsernameOrEmail() { return usernameOrEmail; }
    public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}