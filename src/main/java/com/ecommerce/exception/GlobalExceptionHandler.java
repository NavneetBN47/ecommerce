package com.ecommerce.exception;

import com.ecommerce.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameExists(UsernameExistsException ex, WebRequest request) {
        log.error("Username exists: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("USERNAME_EXISTS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        log.error("Invalid credentials: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        log.error("Unauthorized: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("UNAUTHORIZED")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(ProductNotFoundException ex, WebRequest request) {
        log.error("Product not found: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("PRODUCT_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCartNotFound(CartNotFoundException ex, WebRequest request) {
        log.error("Cart not found: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("CART_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleItemNotFound(ItemNotFoundException ex, WebRequest request) {
        log.error("Item not found: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("ITEM_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQuantity(InvalidQuantityException ex, WebRequest request) {
        log.error("Invalid quantity: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("INVALID_QUANTITY")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidInput(InvalidInputException ex, WebRequest request) {
        log.error("Invalid input: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("INVALID_INPUT")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation error: {}", errors);
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("INVALID_INPUT")
                .message(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: ", ex);
        ApiErrorResponse error = ApiErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}