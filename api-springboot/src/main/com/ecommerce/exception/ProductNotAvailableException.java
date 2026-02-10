package com.ecommerce.exception;

/**
 * Exception thrown when product is not available
 */
public class ProductNotAvailableException extends RuntimeException {
    
    public ProductNotAvailableException(String message) {
        super(message);
    }
    
    public ProductNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}