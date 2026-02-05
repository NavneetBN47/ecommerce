package com.example.controller;

import com.example.dto.*;
import com.example.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Cart operations
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieve current user's shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/v1/cart - userId: {}", userId);
        
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/v1/cart/items - userId: {}, request: {}", userId, request);
        
        CartDTO cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT /api/v1/cart/items/{} - userId: {}, request: {}", itemId, userId, request);
        
        CartDTO cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a product from the shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        log.info("DELETE /api/v1/cart/items/{} - userId: {}", itemId, userId);
        
        CartDTO cart = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the shopping cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /api/v1/cart - userId: {}", userId);
        
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}