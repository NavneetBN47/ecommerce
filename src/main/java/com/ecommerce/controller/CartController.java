package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Cart operations
 * Implements lazy cart creation, auto-delete empty carts, and logout cleanup
 */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Management", description = "APIs for shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get or create cart", description = "Get active cart for user or create new one (lazy creation)")
    public ResponseEntity<ApiResponse<CartDTO>> getOrCreateCart(@PathVariable Long userId) {
        log.info("REST request to get or create cart for user: {}", userId);
        CartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart by ID", description = "Retrieve cart details by ID")
    public ResponseEntity<ApiResponse<CartDTO>> getCartById(@PathVariable Long cartId) {
        log.info("REST request to get cart by ID: {}", cartId);
        CartDTO cart = cartService.getCartById(cartId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active cart", description = "Retrieve active cart for user")
    public ResponseEntity<ApiResponse<CartDTO>> getActiveCart(@PathVariable Long userId) {
        log.info("REST request to get active cart for user: {}", userId);
        CartDTO cart = cartService.getActiveCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Add item to cart", description = "Add product to cart with quantity validation")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("REST request to add item to cart for user: {}", userId);
        CartDTO cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/user/{userId}/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity", description = "Update quantity of item in cart with stock validation")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("REST request to update cart item: {} for user: {}", cartItemId, userId);
        CartDTO cart = cartService.updateCartItemQuantity(userId, cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @DeleteMapping("/user/{userId}/items/{cartItemId}")
    @Operation(summary = "Remove item from cart", description = "Remove item from cart (auto-deletes cart if empty)")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {
        log.info("REST request to remove cart item: {} for user: {}", cartItemId, userId);
        CartDTO cart = cartService.removeItemFromCart(userId, cartItemId);
        if (cart == null) {
            return ResponseEntity.ok(ApiResponse.success("Item removed and cart deleted (was empty)", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear cart", description = "Remove all items from cart and delete cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        log.info("REST request to clear cart for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }

    @PostMapping("/user/{userId}/logout-cleanup")
    @Operation(summary = "Logout cleanup", description = "Clean up carts on user logout")
    public ResponseEntity<ApiResponse<Void>> logoutCleanup(@PathVariable Long userId) {
        log.info("REST request for logout cleanup for user: {}", userId);
        cartService.cleanupOnLogout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logout cleanup completed", null));
    }
}