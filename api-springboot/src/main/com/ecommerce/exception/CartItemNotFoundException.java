package com.ecommerce.exception;

/**
 * Exception thrown when cart item is not found
 */
public class CartItemNotFoundException extends RuntimeException {
    
    public CartItemNotFoundException(String message) {
        super(message);
    }
    
    public CartItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}