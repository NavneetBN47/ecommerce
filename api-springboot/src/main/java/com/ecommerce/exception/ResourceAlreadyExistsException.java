package com.ecommerce.exception;

/**
 * Exception for resource already exists scenarios (409)
 * Implements LLD error handling for conflict cases
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: %s", resource, field, value));
    }
}