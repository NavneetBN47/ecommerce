package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart Controller - REST API endpoints for cart management
 * Implements lazy cart creation and auto-delete empty cart
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Management", description = "APIs for shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get cart for user (lazy creation)")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable Long userId) {
        log.info("REST request to get cart for user ID: {}", userId);
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {
        log.info("REST request to add item to cart for user ID: {}", userId);
        CartDTO cart = cartService.addItemToCart(userId, cartItemDTO);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/user/{userId}/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("REST request to update cart item ID: {} for user ID: {}", itemId, userId);
        CartDTO cart = cartService.updateCartItem(userId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/user/{userId}/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        log.info("REST request to remove cart item ID: {} for user ID: {}", itemId, userId);
        CartDTO cart = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        log.info("REST request to clear cart for user ID: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}