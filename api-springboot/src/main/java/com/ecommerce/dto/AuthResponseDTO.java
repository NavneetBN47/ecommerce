package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response with JWT token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserResponseDTO user;

    public static AuthResponseDTO of(String token, Long expiresIn, UserResponseDTO user) {
        return AuthResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .user(user)
            .build();
    }
}