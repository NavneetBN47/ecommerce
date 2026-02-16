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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for CartController.
 * Tests shopping cart operations including add, update, remove, and retrieve cart items.
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

        cartResponse = new CartResponse();
        cartResponse.setUserId(userId);
        cartResponse.setTotalPrice(BigDecimal.valueOf(100.00));
    }

    /**
     * Test successfully adding a product to cart.
     * Verifies that a product can be added and returns CREATED status.
     */
    @Test
    void testAddProductToCart_Success() {
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.addProductToCart(userId, addToCartRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals(BigDecimal.valueOf(100.00), response.getBody().getTotalPrice());
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding product with null request.
     * Verifies proper handling of null input.
     */
    @Test
    void testAddProductToCart_NullRequest() {
        when(cartService.addProductToCart(any(UUID.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        assertThrows(IllegalArgumentException.class, 
            () -> cartController.addProductToCart(userId, null));
        verify(cartService, times(1)).addProductToCart(eq(userId), eq(null));
    }

    /**
     * Test adding product with invalid quantity.
     * Verifies validation of quantity values.
     */
    @Test
    void testAddProductToCart_InvalidQuantity() {
        addToCartRequest.setQuantity(-1);
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenThrow(new IllegalArgumentException("Quantity must be positive"));

        assertThrows(IllegalArgumentException.class, 
            () -> cartController.addProductToCart(userId, addToCartRequest));
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test successfully updating a cart item.
     * Verifies that cart item quantity can be updated.
     */
    @Test
    void testUpdateCartItem_Success() {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.updateCartItem(userId, itemId, updateCartItemRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating non-existent cart item.
     * Verifies proper exception handling for missing items.
     */
    @Test
    void testUpdateCartItem_ItemNotFound() {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class, 
            () -> cartController.updateCartItem(userId, itemId, updateCartItemRequest));
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating cart item with null request.
     * Verifies proper handling of null input.
     */
    @Test
    void testUpdateCartItem_NullRequest() {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Update request cannot be null"));

        assertThrows(IllegalArgumentException.class, 
            () -> cartController.updateCartItem(userId, itemId, null));
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), eq(null));
    }

    /**
     * Test successfully removing a cart item.
     * Verifies that a cart item can be removed.
     */
    @Test
    void testRemoveCartItem_Success() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenReturn(cartResponse);

        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test removing cart item when cart becomes empty.
     * Verifies NO_CONTENT status when cart is empty after removal.
     */
    @Test
    void testRemoveCartItem_EmptyCart() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenReturn(null);

        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test removing non-existent cart item.
     * Verifies proper exception handling for missing items.
     */
    @Test
    void testRemoveCartItem_ItemNotFound() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class, 
            () -> cartController.removeCartItem(userId, itemId));
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test successfully retrieving cart.
     * Verifies that cart details can be fetched.
     */
    @Test
    void testGetCart_Success() {
        when(cartService.getCart(any(UUID.class))).thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        verify(cartService, times(1)).getCart(eq(userId));
    }

    /**
     * Test retrieving cart for non-existent user.
     * Verifies proper exception handling for invalid user.
     */
    @Test
    void testGetCart_UserNotFound() {
        when(cartService.getCart(any(UUID.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> cartController.getCart(userId));
        verify(cartService, times(1)).getCart(eq(userId));
    }

    /**
     * Test successfully logging out and clearing cart.
     * Verifies that cart is cleared on logout.
     */
    @Test
    void testLogout_Success() {
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<Void> response = cartController.logout(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test logout with null user ID.
     * Verifies proper handling of null user ID.
     */
    @Test
    void testLogout_NullUserId() {
        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(null);

        assertThrows(IllegalArgumentException.class, () -> cartController.logout(null));
        verify(cartService, times(1)).clearCart(null);
    }
}