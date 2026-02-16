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

    /**
     * Set up test data before each test method execution.
     */
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
        cartResponse.setItems(new ArrayList<>());
        cartResponse.setTotalAmount(BigDecimal.valueOf(100.00));
    }

    /**
     * Test successfully adding a product to cart.
     * Verifies that product is added and returns HTTP 201 status.
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
     * Test adding product to cart with null user ID.
     * Verifies proper handling of null user ID.
     */
    @Test
    void testAddProductToCart_NullUserId() {
        when(cartService.addProductToCart(null, addToCartRequest))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.addProductToCart(null, addToCartRequest);
        });
    }

    /**
     * Test adding product to cart with invalid quantity.
     * Verifies validation of quantity field.
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
     * Test adding non-existent product to cart.
     * Verifies handling of invalid product ID.
     */
    @Test
    void testAddProductToCart_ProductNotFound() {
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenThrow(new RuntimeException("Product not found"));

        assertThrows(RuntimeException.class, () -> {
            cartController.addProductToCart(userId, addToCartRequest);
        });
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test successfully updating a cart item.
     * Verifies that cart item quantity is updated correctly.
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
     * Test updating cart item with null item ID.
     * Verifies proper handling of null item ID.
     */
    @Test
    void testUpdateCartItem_NullItemId() {
        when(cartService.updateCartItem(any(UUID.class), eq(null), any(UpdateCartItemRequest.class)))
            .thenThrow(new IllegalArgumentException("Item ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.updateCartItem(userId, null, updateCartItemRequest);
        });
    }

    /**
     * Test updating non-existent cart item.
     * Verifies handling of invalid item ID.
     */
    @Test
    void testUpdateCartItem_ItemNotFound() {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class, () -> {
            cartController.updateCartItem(userId, itemId, updateCartItemRequest);
        });
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating cart item with invalid quantity.
     * Verifies validation of quantity field.
     */
    @Test
    void testUpdateCartItem_InvalidQuantity() {
        UpdateCartItemRequest invalidRequest = new UpdateCartItemRequest();
        invalidRequest.setQuantity(-1);

        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
            .thenThrow(new IllegalArgumentException("Quantity must be greater than 0"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.updateCartItem(userId, itemId, invalidRequest);
        });
    }

    /**
     * Test successfully removing a cart item.
     * Verifies that cart item is removed and returns updated cart.
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
     * Verifies that HTTP 204 No Content is returned for empty cart.
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
     * Verifies handling of invalid item ID.
     */
    @Test
    void testRemoveCartItem_ItemNotFound() {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        assertThrows(RuntimeException.class, () -> {
            cartController.removeCartItem(userId, itemId);
        });
        verify(cartService, times(1)).removeCartItem(eq(userId), eq(itemId));
    }

    /**
     * Test successfully retrieving user's cart.
     * Verifies that cart details are returned correctly.
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
     * Test retrieving cart with null user ID.
     * Verifies proper handling of null user ID.
     */
    @Test
    void testGetCart_NullUserId() {
        when(cartService.getCart(null))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.getCart(null);
        });
    }

    /**
     * Test retrieving empty cart.
     * Verifies that empty cart is returned with zero total.
     */
    @Test
    void testGetCart_EmptyCart() {
        CartResponse emptyCart = new CartResponse();
        emptyCart.setUserId(userId);
        emptyCart.setItems(new ArrayList<>());
        emptyCart.setTotalAmount(BigDecimal.ZERO);

        when(cartService.getCart(any(UUID.class))).thenReturn(emptyCart);

        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalAmount());
    }

    /**
     * Test successfully clearing cart on logout.
     * Verifies that cart is cleared and returns HTTP 200 status.
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

        assertThrows(IllegalArgumentException.class, () -> {
            cartController.logout(null);
        });
    }

    /**
     * Test logout when service throws exception.
     * Verifies proper error handling during cart clearing.
     */
    @Test
    void testLogout_ServiceException() {
        doThrow(new RuntimeException("Failed to clear cart"))
            .when(cartService).clearCart(any(UUID.class));

        assertThrows(RuntimeException.class, () -> {
            cartController.logout(userId);
        });
        verify(cartService, times(1)).clearCart(eq(userId));
    }
}