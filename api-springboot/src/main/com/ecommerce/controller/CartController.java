package com.ecommerce.controller;

import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for cart management operations
 */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Add item to cart
     * POST /api/carts/{cartId}/items
     */
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddItemRequest request) {
        log.info("REST request to add item to cart: cartId={}", cartId);
        CartDTO cart = cartService.addItemToCart(cartId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove item from cart
     * DELETE /api/carts/{cartId}/items/{itemId}
     */
    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartDTO> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId) {
        log.info("REST request to remove item from cart: cartId={}, itemId={}", cartId, itemId);
        CartDTO cart = cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * View cart
     * GET /api/carts/{cartId}
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<CartDTO> viewCart(@PathVariable UUID cartId) {
        log.info("REST request to view cart: cartId={}", cartId);
        CartDTO cart = cartService.viewCart(cartId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Cleanup cart (remove all items)
     * DELETE /api/carts/{cartId}/items
     */
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartDTO> cleanupCart(@PathVariable UUID cartId) {
        log.info("REST request to cleanup cart: cartId={}", cartId);
        CartDTO cart = cartService.cleanupCart(cartId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Get or create cart for user (lazy creation)
     * POST /api/carts/user/{userId}
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getOrCreateCart(@PathVariable UUID userId) {
        log.info("REST request to get or create cart for user: userId={}", userId);
        CartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }

    /**
     * Logout cleanup
     * POST /api/carts/logout/{userId}
     */
    @PostMapping("/logout/{userId}")
    public ResponseEntity<Void> logoutCleanup(@PathVariable UUID userId) {
        log.info("REST request for logout cleanup: userId={}", userId);
        cartService.logoutCleanup(userId);
        return ResponseEntity.noContent().build();
    }
}