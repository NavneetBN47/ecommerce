package com.ecommerce.exception;

/**
 * Exception thrown when product stock is insufficient
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
            productName, requested, available));
    }
}