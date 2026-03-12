package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequestDTO;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.UpdateCartItemRequestDTO;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart Controller
 * Handles shopping cart operations with lazy creation and ephemeral lifecycle
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Management", description = "APIs for shopping cart operations (lazy creation, ephemeral lifecycle)")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Add to Cart", description = "Add product to cart (creates cart lazily if not exists)")
    public ResponseEntity<CartDTO> addToCart(@Valid @RequestBody AddToCartRequestDTO request) {
        log.info("POST /api/cart/add - Add product to cart");
        CartDTO cart = cartService.addToCart(request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    @Operation(summary = "Update Cart Item", description = "Update quantity of product in cart")
    public ResponseEntity<CartDTO> updateCartItem(@Valid @RequestBody UpdateCartItemRequestDTO request) {
        log.info("PUT /api/cart/update - Update cart item quantity");
        CartDTO cart = cartService.updateCartItem(request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{userId}/{productId}")
    @Operation(summary = "Remove from Cart", description = "Remove product from cart (auto-deletes cart if empty)")
    public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        log.info("DELETE /api/cart/remove/{}/{} - Remove product from cart", userId, productId);
        CartDTO cart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "View Cart", description = "View all items in user's cart with totals")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long userId) {
        log.info("GET /api/cart/{} - View cart", userId);
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }
}