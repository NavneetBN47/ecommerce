package com.ecommerce.exception;

/**
 * Exception thrown when product is not found
 */
public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
}