package com.example.controller;

import com.example.dto.AddToCartRequest;
import com.example.dto.ApiResponse;
import com.example.dto.CartDTO;
import com.example.dto.UpdateCartItemRequest;
import com.example.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Cart operations.
 * Implements lazy cart creation and auto-delete when empty.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "APIs for shopping cart management")
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get cart by user ID (lazy creation)")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable Long userId) {
        log.info("Fetching cart for user: {}", userId);
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
    
    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Add item to cart (creates cart if doesn't exist)")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Adding item to cart for user: {}", userId);
        CartDTO cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }
    
    @PutMapping("/user/{userId}/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}", itemId, userId);
        CartDTO cart = cartService.updateCartItem(userId, itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }
    
    @DeleteMapping("/user/{userId}/items/{itemId}")
    @Operation(summary = "Remove item from cart (auto-deletes cart if empty)")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        log.info("Removing item from cart: {} for user: {}", itemId, userId);
        CartDTO cart = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }
    
    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        log.info("Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
    
    @PostMapping("/user/{userId}/cleanup")
    @Operation(summary = "Cleanup cart on logout")
    public ResponseEntity<ApiResponse<Void>> cleanupCartOnLogout(@PathVariable Long userId) {
        log.info("Cleaning up cart on logout for user: {}", userId);
        cartService.cleanupCartOnLogout(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleanup completed", null));
    }
}