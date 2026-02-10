package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequestDTO;
import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.UpdateCartItemRequestDTO;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for cart operations
 * Implements lazy cart creation, auto-delete empty cart, and logout cleanup
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart (lazy creation)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<CartDTO>> getCart(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Get cart request for user: {}", userId);
        CartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(cart));
    }

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponseDTO<CartDTO>> addItemToCart(
            @Valid @RequestBody AddToCartRequestDTO request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Add item to cart request for user: {}", userId);
        CartDTO cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Item added to cart successfully", cart));
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponseDTO<CartDTO>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Update cart item request for user: {}, item: {}", userId, cartItemId);
        CartDTO cart = cartService.updateCartItemQuantity(userId, cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponseDTO.success("Cart item updated successfully", cart));
    }

    /**
     * Remove item from cart (auto-delete empty cart)
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponseDTO<CartDTO>> removeItemFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Remove cart item request for user: {}, item: {}", userId, cartItemId);
        CartDTO cart = cartService.removeItemFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponseDTO.success("Item removed from cart successfully", cart));
    }

    /**
     * Clear cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponseDTO<Void>> clearCart(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Clear cart request for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Cart cleared successfully", null));
    }
}