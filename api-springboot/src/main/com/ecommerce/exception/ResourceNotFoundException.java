package com.ecommerce.exception;

/**
 * Resource Not Found Exception
 * Thrown when a requested resource does not exist
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}