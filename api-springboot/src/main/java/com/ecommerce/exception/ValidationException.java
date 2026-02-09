package com.ecommerce.exception;

/**
 * Exception for validation failures (400)
 * Implements LLD error handling for validation cases
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}