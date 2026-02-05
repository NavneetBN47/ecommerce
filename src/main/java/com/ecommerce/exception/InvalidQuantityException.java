package com.ecommerce.exception;

/**
 * Exception thrown when quantity is invalid
 */
public class InvalidQuantityException extends RuntimeException {
    
    public InvalidQuantityException(String message) {
        super(message);
    }
}