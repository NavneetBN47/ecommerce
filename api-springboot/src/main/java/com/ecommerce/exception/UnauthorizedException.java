package com.ecommerce.exception;

/**
 * Exception for unauthorized access scenarios (401)
 * Implements LLD error handling for authentication cases
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}