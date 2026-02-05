package com.ecommerce.exception;

/**
 * Exception thrown when cart is not found
 */
public class CartNotFoundException extends RuntimeException {
    
    public CartNotFoundException(String message) {
        super(message);
    }
}