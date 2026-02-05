package com.ecommerce.exception;

/**
 * Exception thrown when username already exists
 */
public class UsernameExistsException extends RuntimeException {
    
    public UsernameExistsException(String message) {
        super(message);
    }
}