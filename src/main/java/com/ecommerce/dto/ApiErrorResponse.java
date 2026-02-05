package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for API error responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}