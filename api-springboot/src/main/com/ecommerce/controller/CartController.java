package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
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
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/cart/items - Adding product to cart for user: {}", userId);
        CartResponse response = cartService.addProductToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT /api/cart/items/{} - Updating cart item for user: {}", itemId, userId);
        CartResponse response = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID itemId) {
        log.info("DELETE /api/cart/items/{} - Removing cart item for user: {}", itemId, userId);
        CartResponse response = cartService.removeCartItem(userId, itemId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") UUID userId) {
        log.info("GET /api/cart - Fetching cart for user: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-User-Id") UUID userId) {
        log.info("POST /api/cart/logout - Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}