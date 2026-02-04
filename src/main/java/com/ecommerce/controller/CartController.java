package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestAttribute("userId") Long userId) {
        log.info("GET /api/cart - userId: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addCartItem(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody AddCartItemRequest request) {
        log.info("POST /api/cart/items - userId: {}, productId: {}", userId, request.getProductId());
        CartItemResponse response = cartService.addCartItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PatchMapping("/items/{id}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PATCH /api/cart/items/{} - userId: {}, quantity: {}", id, userId, request.getQuantity());
        CartItemResponse response = cartService.updateCartItem(userId, id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeCartItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        log.info("DELETE /api/cart/items/{} - userId: {}", id, userId);
        cartService.removeCartItem(userId, id);
        return ResponseEntity.noContent().build();
    }
}
