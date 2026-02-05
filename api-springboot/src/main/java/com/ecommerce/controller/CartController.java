package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Shopping Cart operations
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart (lazy creation)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@CurrentUser UserPrincipal currentUser) {
        CartDTO cart = cartService.getOrCreateCart(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        CartDTO cart = cartService.addItemToCart(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartDTO cart = cartService.updateCartItem(currentUser.getUserId(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long cartItemId) {
        CartDTO cart = cartService.removeItemFromCart(currentUser.getUserId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    /**
     * Clear all items from cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}