package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for shopping cart endpoints
 * Implements:
 * - Lazy cart creation
 * - Auto-delete empty cart
 * - Quantity validation
 * - Cart totals calculation
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * Add item to cart
     * POST /api/cart/add
     * Business Rule: Cart is created lazily - only when first item is added
     * Business Rule: Validate stock before adding to cart
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Add to cart request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        CartSummaryResponse response = cartService.addToCart(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", response));
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/{id}
     * Business Rule: Validate stock before updating quantity
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Update cart item request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        CartSummaryResponse response = cartService.updateCartItem(user.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", response));
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/items/{id}
     * Business Rule: Auto-delete empty cart when last item is removed
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        log.info("Remove from cart request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        CartSummaryResponse response = cartService.removeFromCart(user.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", response));
    }

    /**
     * Get cart summary
     * GET /api/cart
     * Business Rule: Calculate subtotal, tax, and grand total
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get cart request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        CartSummaryResponse response = cartService.getCartSummary(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", response));
    }

    /**
     * Get cart summary (alias endpoint)
     * GET /api/cart/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get cart summary request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        CartSummaryResponse response = cartService.getCartSummary(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart summary retrieved successfully", response));
    }

    /**
     * Clear cart
     * DELETE /api/cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Clear cart request received for user: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        cartService.clearCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}