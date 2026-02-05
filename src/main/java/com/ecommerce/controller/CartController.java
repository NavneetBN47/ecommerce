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
 * REST controller for Cart operations
 * Implements lazy cart creation and auto-delete when empty
 */
@RestController
@RequestMapping("/carts")
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

    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {
        CartDTO cart = cartService.addItemToCart(userId, cartItemDTO);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/user/{userId}/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartDTO cart = cartService.updateCartItemQuantity(userId, cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @DeleteMapping("/user/{userId}/items/{cartItemId}")
    @Operation(summary = "Remove item from cart (auto-delete cart if empty)")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {
        CartDTO cart = cartService.removeItemFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear cart (remove all items and delete cart)")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }

    @PostMapping("/user/{userId}/logout")
    @Operation(summary = "Delete cart on logout (cleanup)")
    public ResponseEntity<ApiResponse<Void>> deleteCartOnLogout(@PathVariable Long userId) {
        cartService.deleteCartOnLogout(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart deleted on logout", null));
    }
}