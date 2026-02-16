package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for CartController
 * Tests shopping cart operations including add, update, remove, and retrieve
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_CartController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private UUID userId;
    private UUID productId;
    private UUID itemId;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        
        cartResponse = CartResponse.builder()
            .cartId(UUID.randomUUID())
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.valueOf(100.00))
            .build();
    }

    /**
     * Test adding product to cart successfully
     * Verifies HTTP 201 status and cart response
     */
    @Test
    void addProductToCartShouldReturnCreatedStatus() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
            .thenReturn(cartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.addProductToCart(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding product with invalid quantity
     * Verifies validation exception handling
     */
    @Test
    void addProductToCartShouldHandleInvalidQuantity() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(-1);

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
            .thenThrow(new RuntimeException("Quantity must be greater than 0"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.addProductToCart(userId, request);
        });
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding non-existent product to cart
     * Verifies resource not found exception handling
     */
    @Test
    void addProductToCartShouldHandleNonExistentProduct() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(1);

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
            .thenThrow(new RuntimeException("Product not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.addProductToCart(userId, request);
        });
    }

    /**
     * Test updating cart item quantity successfully
     * Verifies HTTP 200 status and updated cart response
     */
    @Test
    void updateCartItemShouldReturnOkStatus() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.updateCartItem(userId, itemId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating cart item with invalid quantity
     * Verifies validation of quantity parameter
     */
    @Test
    void updateCartItemShouldHandleInvalidQuantity() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Quantity must be greater than 0"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.updateCartItem(userId, itemId, request);
        });
    }

    /**
     * Test updating non-existent cart item
     * Verifies proper exception handling
     */
    @Test
    void updateCartItemShouldHandleNonExistentItem() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.updateCartItem(userId, itemId, request);
        });
    }

    /**
     * Test removing cart item successfully
     * Verifies HTTP 200 status and updated cart
     */
    @Test
    void removeCartItemShouldReturnOkStatus() {
        // Given
        when(cartService.removeCartItem(userId, itemId)).thenReturn(cartResponse);

        // When
        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test removing last item from cart
     * Verifies HTTP 204 No Content when cart becomes empty
     */
    @Test
    void removeCartItemShouldReturnNoContentWhenCartEmpty() {
        // Given
        when(cartService.removeCartItem(userId, itemId)).thenReturn(null);

        // When
        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test removing non-existent cart item
     * Verifies exception handling
     */
    @Test
    void removeCartItemShouldHandleNonExistentItem() {
        // Given
        when(cartService.removeCartItem(userId, itemId))
            .thenThrow(new RuntimeException("Cart item not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.removeCartItem(userId, itemId);
        });
    }

    /**
     * Test getting cart successfully
     * Verifies HTTP 200 status and cart data
     */
    @Test
    void getCartShouldReturnOkStatus() {
        // Given
        when(cartService.getCart(userId)).thenReturn(cartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test getting non-existent cart
     * Verifies exception handling when cart not found
     */
    @Test
    void getCartShouldHandleNonExistentCart() {
        // Given
        when(cartService.getCart(userId))
            .thenThrow(new RuntimeException("Cart not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.getCart(userId);
        });
    }

    /**
     * Test logout clears cart successfully
     * Verifies HTTP 200 status
     */
    @Test
    void logoutShouldReturnOkStatus() {
        // Given
        doNothing().when(cartService).clearCart(userId);

        // When
        ResponseEntity<Void> response = cartController.logout(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with null user ID
     * Verifies validation of user ID parameter
     */
    @Test
    void logoutShouldHandleNullUserId() {
        // Given
        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            cartController.logout(null);
        });
    }

    /**
     * Test adding product to cart with zero quantity
     * Verifies quantity validation
     */
    @Test
    void addProductToCartShouldRejectZeroQuantity() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(0);

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
            .thenThrow(new RuntimeException("Quantity must be greater than 0"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.addProductToCart(userId, request);
        });
    }
}