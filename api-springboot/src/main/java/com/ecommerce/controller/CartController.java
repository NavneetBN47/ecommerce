package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for shopping cart operations
 * Implements LLD cart management API contracts
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "Shopping Cart", description = "APIs for shopping cart management")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * Add product to cart endpoint
     * Implements LLD POST /api/cart/items
     */
    @PostMapping("/items")
    @Operation(summary = "Add product to cart", description = "Add a product to the shopping cart with lazy cart creation")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Add to cart request received for user ID: {}", userId);
        CartResponse response = cartService.addProductToCart(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added to cart", response));
    }

    /**
     * Update cart item endpoint
     * Implements LLD PUT /api/cart/items/{item_id}
     */
    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            Authentication authentication,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Update cart item request received for user ID: {} and item ID: {}", userId, itemId);
        CartResponse response = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", response));
    }

    /**
     * Remove cart item endpoint with auto-delete empty cart
     * Implements LLD DELETE /api/cart/items/{item_id}
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove cart item", description = "Remove item from cart, auto-delete cart if empty")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            Authentication authentication,
            @PathVariable UUID itemId) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Remove cart item request received for user ID: {} and item ID: {}", userId, itemId);
        CartResponse response = cartService.removeCartItem(userId, itemId);

        if (response == null) {
            // Cart was auto-deleted because it became empty
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        }

        return ResponseEntity.ok(ApiResponse.success("Cart item removed", response));
    }

    /**
     * Get cart endpoint
     * Implements LLD GET /api/cart
     */
    @GetMapping
    @Operation(summary = "Get shopping cart", description = "Retrieve user's shopping cart with all items")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Get cart request received for user ID: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}