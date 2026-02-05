package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for shopping cart operations
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
    /**
     * GET /api/cart - Get user's cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@CurrentUser UUID userId) {
        log.info("Get cart request for user: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/cart/items - Add product to cart
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @CurrentUser UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Add to cart request for user: {}", userId);
        CartResponse response = cartService.addProductToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/cart/items/{itemId} - Update cart item quantity
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Update cart item request for user: {}", userId);
        CartResponse response = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/cart/items/{itemId} - Remove item from cart
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @CurrentUser UUID userId,
            @PathVariable UUID itemId) {
        log.info("Remove cart item request for user: {}", userId);
        CartResponse response = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(response);
    }
}