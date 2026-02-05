package com.ecommerce.exception;

/**
 * Exception thrown when product stock is insufficient for requested quantity
 */
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}