package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Shopping Cart", description = "APIs for managing user shopping carts")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
        summary = "Get user's cart",
        description = "Retrieves the authenticated user's shopping cart with all items"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart retrieved successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> getCart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        CartResponse response = cartService.getCart(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the user's shopping cart or updates quantity if already exists"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Item added to cart successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - Invalid quantity or product",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Insufficient stock",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> addItemToCart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @Valid @RequestBody @Parameter(description = "Item to add to cart") AddCartItemRequest request) {
        CartResponse response = cartService.addItemToCart(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(
        summary = "Update cart item quantity",
        description = "Updates the quantity of a specific item in the cart"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart item updated successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid quantity",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Item belongs to another user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart item not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Insufficient stock",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> updateCartItem(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable @Parameter(description = "Cart item ID", example = "1") Long itemId,
            @Valid @RequestBody @Parameter(description = "Updated quantity") UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItem(token, itemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(
        summary = "Remove item from cart",
        description = "Removes a specific item from the user's shopping cart"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item removed successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Item belongs to another user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart item not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> removeItemFromCart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable @Parameter(description = "Cart item ID", example = "1") Long itemId) {
        CartResponse response = cartService.removeItemFromCart(token, itemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(
        summary = "Clear cart",
        description = "Removes all items from the user's shopping cart"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cart cleared successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> clearCart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        cartService.clearCart(token);
        return ResponseEntity.noContent().build();
    }
}