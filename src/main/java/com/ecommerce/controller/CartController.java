package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Cart Controller
 * Handles shopping cart endpoints
 * API Contracts as per LLD Section 4.3
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart
     * Get current user's cart
     * @param authentication current authenticated user
     * @return 200 OK with cart response
     */
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Get cart request for user: {}", userId);
        CartResponseDTO response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/cart/items
     * Add product to cart
     * @param addToCartDTO product and quantity
     * @param authentication current authenticated user
     * @return 201 Created with updated cart response
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            @Valid @RequestBody AddToCartDTO addToCartDTO,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Add to cart request for user: {}", userId);
        CartResponseDTO response = cartService.addProductToCart(userId, addToCartDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/cart/items/{itemId}
     * Update cart item quantity
     * @param itemId cart item ID
     * @param updateDTO new quantity
     * @param authentication current authenticated user
     * @return 200 OK with updated cart response
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemDTO updateDTO,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Update cart item request for user: {}", userId);
        CartResponseDTO response = cartService.updateCartItem(userId, itemId, updateDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/cart/items/{itemId}
     * Remove cart item
     * @param itemId cart item ID
     * @param authentication current authenticated user
     * @return 200 OK with updated cart response or message if cart deleted
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable UUID itemId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Remove cart item request for user: {}", userId);
        CartResponseDTO response = cartService.removeCartItem(userId, itemId);
        
        if (response == null) {
            // Cart was auto-deleted because it was empty
            return ResponseEntity.ok(Map.of("message", "Cart item removed. Cart auto-deleted as it was empty."));
        }
        
        return ResponseEntity.ok(response);
    }
}