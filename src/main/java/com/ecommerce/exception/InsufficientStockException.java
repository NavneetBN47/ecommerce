package com.ecommerce.exception;

/**
 * Exception thrown when there is insufficient stock for a product
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}