package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart Controller
 * REST API endpoints for shopping cart management
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * Add product to cart
     * POST /api/cart/items
     * Note: In production, userId should come from JWT token
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        logger.info("POST /api/cart/items - User ID: {}, Product ID: {}", userId, request.getProductId());
        CartResponse response = cartService.addProductToCart(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/{itemId}
     * Note: In production, userId should come from JWT token
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        logger.info("PUT /api/cart/items/{} - User ID: {}", itemId, userId);
        CartResponse response = cartService.updateCartItemQuantity(userId, itemId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove product from cart
     * DELETE /api/cart/items/{itemId}
     * Note: In production, userId should come from JWT token
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeProductFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        logger.info("DELETE /api/cart/items/{} - User ID: {}", itemId, userId);
        CartResponse response = cartService.removeProductFromCart(userId, itemId);
        return ResponseEntity.ok(response);
    }

    /**
     * View cart
     * GET /api/cart
     * Note: In production, userId should come from JWT token
     */
    @GetMapping
    public ResponseEntity<CartResponse> viewCart(@RequestHeader("X-User-Id") Long userId) {
        logger.info("GET /api/cart - User ID: {}", userId);
        CartResponse response = cartService.viewCart(userId);
        return ResponseEntity.ok(response);
    }
}