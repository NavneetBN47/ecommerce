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
 * Test class for CartController
 * 
 * Tests cart operations including add, update, remove, and retrieve cart items
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
    private UUID itemId;
    private UUID productId;
    private AddToCartRequest addToCartRequest;
    private UpdateCartItemRequest updateCartItemRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        productId = UUID.randomUUID();

        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setProductId(productId);
        addToCartRequest.setQuantity(2);

        updateCartItemRequest = new UpdateCartItemRequest();
        updateCartItemRequest.setQuantity(3);

        cartResponse = CartResponse.builder()
            .cartId(UUID.randomUUID())
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.valueOf(100.00))
            .build();
    }

    /**
     * Test adding product to cart successfully
     * 
     * Verifies:
     * - Product is added to cart
     * - Returns CREATED status
     * - CartService.addProductToCart is called with correct parameters
     */
    @Test
    void testAddProductToCart_Success() {
        // Given
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenReturn(cartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.addProductToCart(userId, addToCartRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be CREATED");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).addProductToCart(userId, addToCartRequest);
    }

    /**
     * Test adding product with invalid quantity
     * 
     * Verifies:
     * - Validation rejects zero or negative quantities
     */
    @Test
    void testAddProductToCart_InvalidQuantity() {
        // Given
        addToCartRequest.setQuantity(0);
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenThrow(new RuntimeException("Quantity must be greater than 0"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.addProductToCart(userId, addToCartRequest);
        });
    }

    /**
     * Test updating cart item successfully
     * 
     * Verifies:
     * - Cart item quantity is updated
     * - Returns OK status
     * - CartService.updateCartItem is called with correct parameters
     */
    @Test
    void testUpdateCartItem_Success() {
        // Given
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.updateCartItem(userId, itemId, updateCartItemRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).updateCartItem(userId, itemId, updateCartItemRequest);
    }

    /**
     * Test updating non-existent cart item
     * 
     * Verifies:
     * - Appropriate exception is thrown for non-existent item
     */
    @Test
    void testUpdateCartItem_ItemNotFound() {
        // Given
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cartController.updateCartItem(userId, itemId, updateCartItemRequest);
        });
    }

    /**
     * Test removing cart item successfully
     * 
     * Verifies:
     * - Cart item is removed
     * - Returns OK status with updated cart
     * - CartService.removeCartItem is called with correct parameters
     */
    @Test
    void testRemoveCartItem_Success() {
        // Given
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenReturn(cartResponse);

        // When
        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test removing last item from cart
     * 
     * Verifies:
     * - Cart is deleted when last item is removed
     * - Returns NO_CONTENT status
     */
    @Test
    void testRemoveCartItem_LastItem() {
        // Given
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenReturn(null);

        // When
        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Status should be NO_CONTENT");
        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test retrieving cart successfully
     * 
     * Verifies:
     * - Cart is retrieved with all items
     * - Returns OK status
     * - CartService.getCart is called with correct user ID
     */
    @Test
    void testGetCart_Success() {
        // Given
        when(cartService.getCart(any(UUID.class))).thenReturn(cartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(cartResponse, response.getBody());
        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test retrieving empty cart
     * 
     * Verifies:
     * - Empty cart is handled appropriately
     */
    @Test
    void testGetCart_EmptyCart() {
        // Given
        CartResponse emptyCart = CartResponse.builder()
            .cartId(UUID.randomUUID())
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.ZERO)
            .build();
        when(cartService.getCart(any(UUID.class))).thenReturn(emptyCart);

        // When
        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertTrue(response.getBody().getItems().isEmpty(), "Cart should be empty");
        assertEquals(BigDecimal.ZERO, response.getBody().getGrandTotal());
    }

    /**
     * Test logout clears cart
     * 
     * Verifies:
     * - Cart is cleared on logout
     * - Returns OK status
     * - CartService.clearCart is called with correct user ID
     */
    @Test
    void testLogout_Success() {
        // Given
        doNothing().when(cartService).clearCart(any(UUID.class));

        // When
        ResponseEntity<Void> response = cartController.logout(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        verify(cartService, times(1)).clearCart(userId);
    }
}
