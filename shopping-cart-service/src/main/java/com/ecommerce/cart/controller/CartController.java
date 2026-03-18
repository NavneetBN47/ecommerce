package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart and cart items")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @PostMapping("/users/{userId}/items")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the user's shopping cart. If the product already exists, increases the quantity.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item added to cart successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<CartResponse> addToCart(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId,
        @Valid @RequestBody AddToCartRequest request
    ) {
        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/{userId}")
    @Operation(
        summary = "Get user's cart",
        description = "Retrieves the shopping cart for the specified user with all items and total amount.",
        security = @SecurityRequirement(name = "bearerAuth")
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
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found",
            content = @Content
        )
    })
    public ResponseEntity<CartResponse> getCart(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/users/{userId}/items/{itemId}")
    @Operation(
        summary = "Update cart item quantity",
        description = "Updates the quantity of a specific item in the cart. Set quantity to 0 to remove the item.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart item updated successfully",
            content = @Content(schema = @Schema(implementation = CartResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart or item not found",
            content = @Content
        )
    })
    public ResponseEntity<CartResponse> updateCartItemQuantity(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId,
        @Parameter(description = "Cart item ID", required = true, example = "1")
        @PathVariable Long itemId,
        @Parameter(description = "New quantity", required = true, example = "3")
        @RequestParam Integer quantity
    ) {
        CartResponse response = cartService.updateCartItemQuantity(userId, itemId, quantity);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/users/{userId}/items/{itemId}")
    @Operation(
        summary = "Remove item from cart",
        description = "Removes a specific item from the user's shopping cart.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Item removed successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart or item not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> removeFromCart(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId,
        @Parameter(description = "Cart item ID", required = true, example = "1")
        @PathVariable Long itemId
    ) {
        cartService.removeFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/users/{userId}")
    @Operation(
        summary = "Clear cart",
        description = "Removes all items from the user's shopping cart.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cart cleared successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token",
            content = @Content
        )
    })
    public ResponseEntity<Void> clearCart(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}