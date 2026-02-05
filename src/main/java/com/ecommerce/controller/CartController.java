package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.UpdateCartItemRequest;
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
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    @Operation(summary = "Get or create user cart")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@CurrentUser Long userId) {
        log.info("GET /api/cart - User: {}", userId);
        CartDTO cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
    
    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @CurrentUser Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/cart/items - User: {}, Product: {}", userId, request.getProductId());
        CartDTO cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }
    
    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @CurrentUser Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT /api/cart/items/{} - User: {}, Quantity: {}", cartItemId, userId, request.getQuantity());
        CartDTO cart = cartService.updateCartItem(userId, cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }
    
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @CurrentUser Long userId,
            @PathVariable Long cartItemId) {
        log.info("DELETE /api/cart/items/{} - User: {}", cartItemId, userId);
        CartDTO cart = cartService.removeItemFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }
    
    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser Long userId) {
        log.info("DELETE /api/cart - User: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}