package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.dto.AddCartItemRequest;
import com.ecommerce.shoppingcart.application.dto.CartResponse;
import com.ecommerce.shoppingcart.application.dto.UpdateCartItemRequest;
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
@Tag(name = "Shopping Cart", description = "APIs for shopping cart management with ABAC authorization and circuit breaker patterns")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get user's cart",
        description = "Retrieves the authenticated user's shopping cart with all items, prices, and totals. Cart is created lazily if it doesn't exist."
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
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error or service unavailable",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<CartResponse> getCart(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId
    ) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the user's cart with specified quantity. Validates product availability and stock via Product Service (with circuit breaker). Creates cart if it doesn't exist."
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
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Product service unavailable (circuit breaker open)",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<CartResponse> addItemToCart(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId,
        
        @Parameter(description = "Product and quantity to add", required = true)
        @Valid @RequestBody AddCartItemRequest request
    ) {
        CartResponse response = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update cart item quantity",
        description = "Updates the quantity of an existing cart item. Validates stock availability before update. Enforces ABAC - users can only update their own cart items."
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
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - cart item belongs to another user",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart item not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<CartResponse> updateCartItem(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId,
        
        @Parameter(description = "Cart item ID to update", required = true, example = "123")
        @PathVariable Long cartItemId,
        
        @Parameter(description = "New quantity", required = true)
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartService.updateCartItem(userId, cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Remove item from cart",
        description = "Removes a specific item from the user's cart. Enforces ABAC - users can only remove items from their own cart."
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
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - cart item belongs to another user",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart item not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<CartResponse> removeItemFromCart(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId,
        
        @Parameter(description = "Cart item ID to remove", required = true, example = "123")
        @PathVariable Long cartItemId
    ) {
        CartResponse response = cartService.removeItemFromCart(userId, cartItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Clear cart",
        description = "Removes all items from the user's cart. The cart entity itself is retained for future use."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cart cleared successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<Void> clearCart(
        @Parameter(description = "User ID from JWT token", hidden = true)
        @RequestAttribute("userId") Long userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}