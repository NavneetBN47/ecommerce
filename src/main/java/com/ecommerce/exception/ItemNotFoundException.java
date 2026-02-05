package com.ecommerce.exception;

/**
 * Exception thrown when cart item is not found
 */
public class ItemNotFoundException extends RuntimeException {
    
    public ItemNotFoundException(String message) {
        super(message);
    }
}