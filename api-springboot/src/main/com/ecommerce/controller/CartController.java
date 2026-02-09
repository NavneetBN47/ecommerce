package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Received add to cart request for user ID: {}", userId);
        CartResponse response = cartService.addProductToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Product added to cart", response));
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Received update cart item request for item ID: {}", itemId);
        CartResponse response = cartService.updateCartItem(userId, itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", response));
    }
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID itemId) {
        log.info("Received remove cart item request for item ID: {}", itemId);
        CartResponse response = cartService.removeCartItem(userId, itemId);
        
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Cart item removed", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Received get cart request for user ID: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}