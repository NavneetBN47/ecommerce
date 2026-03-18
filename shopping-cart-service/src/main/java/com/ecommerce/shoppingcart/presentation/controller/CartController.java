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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart operations including add, update, remove items and cart retrieval")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
        summary = "Get user's shopping cart",
        description = "Retrieves the authenticated user's shopping cart with all items, prices, and total amount. Cart is created lazily if it doesn't exist."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart retrieved successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> getCart(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the user's shopping cart with specified quantity. Validates product availability and stock before adding."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Item added to cart successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - product not available or insufficient stock",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Service unavailable - Product service is down",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> addItemToCart(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails,
        @Valid @RequestBody @Parameter(description = "Product and quantity to add", required = true) AddCartItemRequest request
    ) {
        Long userId = extractUserId(userDetails);
        CartResponse response = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(
        summary = "Update cart item quantity",
        description = "Updates the quantity of an existing cart item. Validates stock availability before updating."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart item updated successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid quantity or insufficient stock",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Item does not belong to user's cart",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart item not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> updateCartItem(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails,
        @PathVariable @Parameter(description = "Cart item ID", required = true) Long itemId,
        @Valid @RequestBody @Parameter(description = "New quantity", required = true) UpdateCartItemRequest request
    ) {
        Long userId = extractUserId(userDetails);
        CartResponse response = cartService.updateCartItem(userId, itemId, request);
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
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Item does not belong to user's cart",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart item not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartResponse> removeItemFromCart(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails,
        @PathVariable @Parameter(description = "Cart item ID to remove", required = true) Long itemId
    ) {
        Long userId = extractUserId(userDetails);
        CartResponse response = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(
        summary = "Clear shopping cart",
        description = "Removes all items from the user's shopping cart"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cart cleared successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> clearCart(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get cart summary",
        description = "Retrieves a summary of the cart including total items count and total amount"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = CartSummaryResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CartSummaryResponse> getCartSummary(
        @AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        CartSummaryResponse response = cartService.getCartSummary(userId);
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(UserDetails userDetails) {
        // Extract user ID from UserDetails (implementation depends on your UserDetails implementation)
        return Long.parseLong(userDetails.getUsername());
    }
}

class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String timestamp;
}

class CartSummaryResponse {
    private Long cartId;
    private Integer totalItems;
    private Double totalAmount;
}