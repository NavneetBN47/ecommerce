package com.ecommerce.exception;

/**
 * Error codes for business exceptions
 * Maps to LLD error code specifications
 */
public enum ErrorCode {
    USERNAME_EXISTS("USERNAME_EXISTS", "Username already exists"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid username or password"),
    INVALID_INPUT("INVALID_INPUT", "Invalid input data"),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found"),
    INVALID_QUANTITY("INVALID_QUANTITY", "Invalid quantity"),
    CART_NOT_FOUND("CART_NOT_FOUND", "Cart not found"),
    ITEM_NOT_FOUND("ITEM_NOT_FOUND", "Cart item not found"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}