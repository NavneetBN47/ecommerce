package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received get cart request for user ID: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AddToCartRequest request) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received add to cart request for user ID: {}", userId);
        CartResponse response = cartService.addProductToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received update cart item request for user ID: {} and item ID: {}", userId, id);
        CartResponse response = cartService.updateCartItem(userId, id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeCartItem(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID id) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received remove cart item request for user ID: {} and item ID: {}", userId, id);
        cartService.removeCartItem(userId, id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> clearCartOnLogout(@RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        log.info("Received logout request for user ID: {}", userId);
        cartService.clearCartOnLogout(userId);
        return ResponseEntity.noContent().build();
    }
    
    private UUID extractUserIdFromToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwtToken);
        return UUID.fromString(userId);
    }
}