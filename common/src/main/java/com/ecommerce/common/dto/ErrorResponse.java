package com.ecommerce.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response structure")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type or category", example = "BAD_REQUEST")
    private String error;

    @Schema(description = "Detailed error message", example = "Invalid input data")
    private String message;

    @Schema(description = "API path where the error occurred", example = "/api/v1/users/register")
    private String path;

    @Schema(description = "List of validation errors (if applicable)")
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Validation error details")
    public static class ValidationError {
        @Schema(description = "Field name that failed validation", example = "email")
        private String field;

        @Schema(description = "Validation error message", example = "Email must be valid")
        private String message;
    }
}