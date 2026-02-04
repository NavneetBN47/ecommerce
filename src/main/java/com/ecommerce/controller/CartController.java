package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestAttribute("userId") UUID userId) {
        log.info("GET /api/cart - Get cart for user ID: {}", userId);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/cart/items - Add product to cart for user ID: {}", userId);
        CartResponse response = cartService.addProductToCart(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @RequestAttribute("userId") UUID userId,
            @PathVariable("id") UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT /api/cart/items/{} - Update cart item quantity for user ID: {}", itemId, userId);
        CartResponse response = cartService.updateCartItemQuantity(userId, itemId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeCartItem(
            @RequestAttribute("userId") UUID userId,
            @PathVariable("id") UUID itemId) {
        log.info("DELETE /api/cart/items/{} - Remove cart item for user ID: {}", itemId, userId);
        cartService.removeCartItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> clearCartOnLogout(@RequestAttribute("userId") UUID userId) {
        log.info("POST /api/cart/logout - Clear cart on logout for user ID: {}", userId);
        cartService.clearCartOnLogout(userId);
        return ResponseEntity.noContent().build();
    }
}