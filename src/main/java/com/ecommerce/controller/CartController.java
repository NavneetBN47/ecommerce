package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Cart operations
 */
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user cart", description = "Get or create cart for current user (lazy creation)")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@CurrentUser Long userId) {
        log.info("Get cart request for user ID: {}", userId);
        CartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add product to cart with quantity validation")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @CurrentUser Long userId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {
        log.info("Add item to cart request for user ID: {}, product ID: {}", 
            userId, cartItemDTO.getProductId());
        CartDTO cart = cartService.addItemToCart(userId, cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item quantity", description = "Update quantity of item in cart")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItemQuantity(
            @CurrentUser Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        log.info("Update cart item quantity request for user ID: {}, product ID: {}, quantity: {}", 
            userId, productId, quantity);
        CartDTO cart = cartService.updateCartItemQuantity(userId, productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Remove product from cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @CurrentUser Long userId,
            @PathVariable Long productId) {
        log.info("Remove item from cart request for user ID: {}, product ID: {}", userId, productId);
        CartDTO cart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser Long userId) {
        log.info("Clear cart request for user ID: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}