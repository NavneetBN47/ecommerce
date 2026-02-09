package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Cart Controller - REST API endpoints for shopping cart management
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    /**
     * POST /api/cart/items - Add product to cart
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Add to cart request for user: {}", currentUser.getId());
        CartResponse response = cartService.addProductToCart(currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/cart/items/{itemId} - Update cart item quantity
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Update cart item request for user: {}, item: {}", currentUser.getId(), itemId);
        CartResponse response = cartService.updateCartItem(currentUser.getId(), itemId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/cart/items/{itemId} - Remove item from cart
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID itemId) {
        log.info("Remove cart item request for user: {}, item: {}", currentUser.getId(), itemId);
        CartResponse response = cartService.removeCartItem(currentUser.getId(), itemId);
        
        if (response.getCartId() == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/cart - Get user's cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@CurrentUser UserPrincipal currentUser) {
        log.info("Get cart request for user: {}", currentUser.getId());
        CartResponse response = cartService.getCart(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}