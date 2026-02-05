package com.ecommerce.exception;

/**
 * Exception thrown for invalid input data
 */
public class InvalidInputException extends RuntimeException {
    
    public InvalidInputException(String message) {
        super(message);
    }
}