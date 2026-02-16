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
 * Unit test class for CartController
 * Tests cart operations including add, update, remove, and retrieve cart items
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
        cartResponse.setTotalAmount(BigDecimal.valueOf(100.00));
    }

    /**
     * Test adding a product to cart successfully
     * Verifies that a product can be added to the cart
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
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding product with null request
     * Verifies proper handling of null input
     */
    @Test
    void testAddProductToCart_NullRequest() {
        when(cartService.addProductToCart(any(UUID.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.addProductToCart(userId, null);
        });
    }

    /**
     * Test adding product with invalid quantity
     * Verifies validation of quantity field
     */
    @Test
    void testAddProductToCart_InvalidQuantity() {
        addToCartRequest.setQuantity(-1);
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenThrow(new IllegalArgumentException("Quantity must be positive"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.addProductToCart(userId, addToCartRequest);
        });
    }

    /**
     * Test updating cart item successfully
     * Verifies that cart item quantity can be updated
     */
    @Test
    void testUpdateCartItem_Success() {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.updateCartItem(userId, itemId, updateCartItemRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating non-existent cart item
     * Verifies proper error handling for non-existent items
     */
    @Test
    void testUpdateCartItem_ItemNotFound() {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class, () -> {
            cartController.updateCartItem(userId, itemId, updateCartItemRequest);
        });
    }

    /**
     * Test removing cart item successfully
     * Verifies that a cart item can be removed
     */
    @Test
    void testRemoveCartItem_Success() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenReturn(cartResponse);

        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test removing cart item returns no content
     * Verifies proper handling when cart becomes empty
     */
    @Test
    void testRemoveCartItem_NoContent() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenReturn(null);

        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test getting cart successfully
     * Verifies that user's cart can be retrieved
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
     * Test getting empty cart
     * Verifies proper handling of empty cart
     */
    @Test
    void testGetCart_EmptyCart() {
        CartResponse emptyCart = new CartResponse();
        emptyCart.setUserId(userId);
        emptyCart.setTotalAmount(BigDecimal.ZERO);
        when(cartService.getCart(any(UUID.class))).thenReturn(emptyCart);

        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalAmount());
    }

    /**
     * Test logout and clear cart successfully
     * Verifies that cart is cleared on logout
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
     * Test logout with null user ID
     * Verifies proper handling of null user ID
     */
    @Test
    void testLogout_NullUserId() {
        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(null);

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.logout(null);
        });
    }
}