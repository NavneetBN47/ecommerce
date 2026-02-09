package com.ecommerce.exception;

/**
 * Exception for resource not found scenarios (404)
 * Implements LLD error handling for not found cases
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value));
    }
}