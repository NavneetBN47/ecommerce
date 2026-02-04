package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestAttribute("userId") String userId) {
        CartResponse response = cartService.getCart(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addProductToCart(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItemQuantity(UUID.fromString(userId), cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeCartItem(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") UUID cartItemId) {
        cartService.removeCartItem(UUID.fromString(userId), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> clearCartOnLogout(@RequestAttribute("userId") String userId) {
        cartService.clearCartOnLogout(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}