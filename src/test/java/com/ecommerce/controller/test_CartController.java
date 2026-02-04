package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit 5 test class for CartController.
 * Tests all public endpoints with proper mocking of CartService dependency.
 * Covers normal execution paths, edge cases, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Tests")
class test_CartController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Test successful retrieval of user cart.
     * Verifies that the controller returns cart response with OK status.
     */
    @Test
    @DisplayName("Should return cart successfully for valid user")
    void testGetCart_Success() {
        // Given
        Long userId = 1L;
        CartItemResponse cartItem = CartItemResponse.builder()
            .cartItemId(1L)
            .productId(1L)
            .productName("Test Product")
            .price(BigDecimal.valueOf(10.00))
            .quantity(2)
            .itemTotal(BigDecimal.valueOf(20.00))
            .build();
        
        CartResponse expectedResponse = CartResponse.builder()
            .cartId(1L)
            .userId(userId)
            .items(Arrays.asList(cartItem))
            .cartTotal(BigDecimal.valueOf(20.00))
            .itemCount(1)
            .build();

        when(cartService.getCart(userId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test retrieval of empty cart.
     * Verifies that the controller handles empty cart scenario correctly.
     */
    @Test
    @DisplayName("Should return empty cart for user with no items")
    void testGetCart_EmptyCart() {
        // Given
        Long userId = 1L;
        CartResponse emptyCartResponse = CartResponse.builder()
            .userId(userId)
            .items(List.of())
            .cartTotal(BigDecimal.ZERO)
            .itemCount(0)
            .build();

        when(cartService.getCart(userId)).thenReturn(emptyCartResponse);

        // When
        ResponseEntity<CartResponse> response = cartController.getCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyCartResponse, response.getBody());
        assertEquals(0, response.getBody().getItemCount());
        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test successful addition of cart item.
     * Verifies that the controller returns created status with cart item response.
     */
    @Test
    @DisplayName("Should add cart item successfully")
    void testAddCartItem_Success() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        CartItemResponse expectedResponse = CartItemResponse.builder()
            .cartItemId(1L)
            .productId(1L)
            .productName("Test Product")
            .price(BigDecimal.valueOf(10.00))
            .quantity(2)
            .itemTotal(BigDecimal.valueOf(20.00))
            .build();

        when(cartService.addCartItem(userId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CartItemResponse> response = cartController.addCartItem(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(cartService, times(1)).addCartItem(userId, request);
    }

    /**
     * Test adding cart item with minimum quantity.
     * Verifies edge case handling for minimum valid quantity.
     */
    @Test
    @DisplayName("Should add cart item with minimum quantity")
    void testAddCartItem_MinimumQuantity() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        CartItemResponse expectedResponse = CartItemResponse.builder()
            .cartItemId(1L)
            .productId(1L)
            .productName("Test Product")
            .price(BigDecimal.valueOf(10.00))
            .quantity(1)
            .itemTotal(BigDecimal.valueOf(10.00))
            .build();

        when(cartService.addCartItem(userId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CartItemResponse> response = cartController.addCartItem(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getQuantity());
        verify(cartService, times(1)).addCartItem(userId, request);
    }

    /**
     * Test successful update of cart item.
     * Verifies that the controller returns updated cart item with OK status.
     */
    @Test
    @DisplayName("Should update cart item successfully")
    void testUpdateCartItem_Success() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        CartItemResponse expectedResponse = CartItemResponse.builder()
            .cartItemId(cartItemId)
            .productId(1L)
            .productName("Test Product")
            .price(BigDecimal.valueOf(10.00))
            .quantity(3)
            .itemTotal(BigDecimal.valueOf(30.00))
            .build();

        when(cartService.updateCartItem(userId, cartItemId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CartItemResponse> response = cartController.updateCartItem(userId, cartItemId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(3, response.getBody().getQuantity());
        verify(cartService, times(1)).updateCartItem(userId, cartItemId, request);
    }

    /**
     * Test updating cart item to zero quantity.
     * Verifies that zero quantity updates are handled properly.
     */
    @Test
    @DisplayName("Should handle zero quantity update")
    void testUpdateCartItem_ZeroQuantity() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        CartItemResponse expectedResponse = CartItemResponse.builder()
            .cartItemId(cartItemId)
            .productId(1L)
            .productName("Test Product")
            .price(BigDecimal.valueOf(10.00))
            .quantity(0)
            .itemTotal(BigDecimal.ZERO)
            .build();

        when(cartService.updateCartItem(userId, cartItemId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CartItemResponse> response = cartController.updateCartItem(userId, cartItemId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getQuantity());
        verify(cartService, times(1)).updateCartItem(userId, cartItemId, request);
    }

    /**
     * Test successful removal of cart item.
     * Verifies that the controller returns no content status after removal.
     */
    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;

        CartItemResponse removedItem = CartItemResponse.builder()
            .cartItemId(cartItemId)
            .productId(1L)
            .productName("Test Product")
            .price(BigDecimal.valueOf(10.00))
            .quantity(2)
            .itemTotal(BigDecimal.valueOf(20.00))
            .build();

        when(cartService.removeCartItem(userId, cartItemId)).thenReturn(removedItem);

        // When
        ResponseEntity<Void> response = cartController.removeCartItem(userId, cartItemId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(cartService, times(1)).removeCartItem(userId, cartItemId);
    }

    /**
     * Test removal of non-existent cart item.
     * Verifies that service layer exceptions are properly propagated.
     */
    @Test
    @DisplayName("Should handle removal of non-existent cart item")
    void testRemoveCartItem_NotFound() {
        // Given
        Long userId = 1L;
        Long cartItemId = 999L;

        when(cartService.removeCartItem(userId, cartItemId))
            .thenThrow(new RuntimeException("Cart item not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cartController.removeCartItem(userId, cartItemId);
        });
        
        verify(cartService, times(1)).removeCartItem(userId, cartItemId);
    }

    /**
     * Test cart operations with null user ID.
     * Verifies that null user ID scenarios are handled appropriately.
     */
    @Test
    @DisplayName("Should handle null user ID")
    void testGetCart_NullUserId() {
        // Given
        Long userId = null;

        when(cartService.getCart(userId)).thenThrow(new IllegalArgumentException("User ID cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cartController.getCart(userId);
        });
        
        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test adding cart item with null request.
     * Verifies that null request scenarios are handled appropriately.
     */
    @Test
    @DisplayName("Should handle null add cart item request")
    void testAddCartItem_NullRequest() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = null;

        when(cartService.addCartItem(userId, request))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cartController.addCartItem(userId, request);
        });
        
        verify(cartService, times(1)).addCartItem(userId, request);
    }
}