package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart REST Controller with lazy creation and auto-cleanup
 */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs for managing shopping carts")
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get or create cart for user (lazy creation)")
    public ResponseEntity<ApiResponse<CartDTO>> getOrCreateCart(@PathVariable Long userId) {
        CartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart by ID")
    public ResponseEntity<ApiResponse<CartDTO>> getCartById(@PathVariable Long cartId) {
        CartDTO cart = cartService.getCartById(cartId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Add item to cart with stock check")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemDTO itemDTO) {
        CartDTO cart = cartService.addItemToCart(userId, itemDTO);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        CartDTO cart = cartService.updateCartItem(cartId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Remove item from cart (auto-delete if empty)")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {
        CartDTO cart = cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}