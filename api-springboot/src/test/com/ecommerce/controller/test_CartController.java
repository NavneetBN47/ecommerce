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
 * JUnit 5 test class for CartController.
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
        cartResponse.setTotalPrice(BigDecimal.valueOf(99.99));
    }

    /**
     * Test adding a product to cart successfully.
     * Verifies that product is added and returns HTTP 201 CREATED status.
     */
    @Test
    void testAddProductToCart_Success() {
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class))).thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.addProductToCart(userId, addToCartRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals(BigDecimal.valueOf(99.99), response.getBody().getTotalPrice());
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding product with null request.
     * Verifies proper exception handling.
     */
    @Test
    void testAddProductToCart_NullRequest() {
        assertThrows(Exception.class, () -> {
            cartController.addProductToCart(userId, null);
        });
    }

    /**
     * Test adding product with invalid quantity.
     * Verifies validation logic.
     */
    @Test
    void testAddProductToCart_InvalidQuantity() {
        AddToCartRequest invalidRequest = new AddToCartRequest();
        invalidRequest.setProductId(productId);
        invalidRequest.setQuantity(0);

        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenThrow(new IllegalArgumentException("Quantity must be greater than 0"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.addProductToCart(userId, invalidRequest);
        });
    }

    /**
     * Test updating cart item successfully.
     * Verifies that item quantity is updated and returns HTTP 200 OK status.
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
     * Test updating non-existent cart item.
     * Verifies proper error handling.
     */
    @Test
    void testUpdateCartItem_ItemNotFound() {
        UUID nonExistentItemId = UUID.randomUUID();
        when(cartService.updateCartItem(any(UUID.class), eq(nonExistentItemId), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class, () -> {
            cartController.updateCartItem(userId, nonExistentItemId, updateCartItemRequest);
        });
    }

    /**
     * Test removing cart item successfully.
     * Verifies that item is removed and returns HTTP 200 OK status.
     */
    @Test
    void testRemoveCartItem_Success() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class))).thenReturn(cartResponse);

        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test removing cart item when cart becomes empty.
     * Verifies that HTTP 204 NO CONTENT is returned.
     */
    @Test
    void testRemoveCartItem_EmptyCart() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class))).thenReturn(null);

        ResponseEntity<?> response = cartController.removeCartItem(userId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test getting cart successfully.
     * Verifies that cart details are retrieved with HTTP 200 OK status.
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
     * Test getting cart for non-existent user.
     * Verifies proper error handling.
     */
    @Test
    void testGetCart_UserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(cartService.getCart(eq(nonExistentUserId)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            cartController.getCart(nonExistentUserId);
        });
    }

    /**
     * Test logout and cart clearing successfully.
     * Verifies that cart is cleared and returns HTTP 200 OK status.
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
     * Verifies proper null handling.
     */
    @Test
    void testLogout_NullUserId() {
        assertThrows(Exception.class, () -> {
            cartController.logout(null);
        });
    }
}