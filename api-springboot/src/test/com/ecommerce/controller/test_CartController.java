package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for CartController
 * Tests shopping cart operations including add, update, remove, and retrieve cart items
 */
@WebMvcTest(CartController.class)
@DisplayName("CartController Tests")
class test_CartController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private UUID testItemId;
    private UUID testProductId;
    private AddToCartRequest addToCartRequest;
    private UpdateCartItemRequest updateCartItemRequest;
    private CartResponse cartResponse;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setProductId(testProductId);
        addToCartRequest.setQuantity(2);

        updateCartItemRequest = new UpdateCartItemRequest();
        updateCartItemRequest.setQuantity(3);

        cartResponse = new CartResponse();
        cartResponse.setUserId(testUserId);
        cartResponse.setItems(new ArrayList<>());
        cartResponse.setTotalPrice(BigDecimal.valueOf(100.00));
        cartResponse.setTotalItems(2);
    }

    /**
     * Test successfully adding product to cart
     * Verifies that valid add request returns 201 CREATED with cart response
     */
    @Test
    @DisplayName("Should add product to cart successfully")
    void testAddProductToCart_Success() throws Exception {
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(post("/api/cart/items")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(100.00));

        verify(cartService, times(1)).addProductToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    /**
     * Test adding product with invalid quantity
     * Verifies that validation catches invalid quantity
     */
    @Test
    @DisplayName("Should return 400 when quantity is invalid")
    void testAddProductToCart_InvalidQuantity() throws Exception {
        AddToCartRequest invalidRequest = new AddToCartRequest();
        invalidRequest.setProductId(testProductId);
        invalidRequest.setQuantity(-1);

        mockMvc.perform(post("/api/cart/items")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addProductToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    /**
     * Test adding product without user ID header
     * Verifies that missing user ID is handled
     */
    @Test
    @DisplayName("Should return 400 when X-User-Id header is missing")
    void testAddProductToCart_MissingUserId() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addProductToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    /**
     * Test successfully updating cart item
     * Verifies that valid update request returns updated cart
     */
    @Test
    @DisplayName("Should update cart item successfully")
    void testUpdateCartItem_Success() throws Exception {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(put("/api/cart/items/{itemId}", testItemId)
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.totalItems").value(2));

        verify(cartService, times(1)).updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating non-existent cart item
     * Verifies that service exception is properly handled
     */
    @Test
    @DisplayName("Should return error when updating non-existent item")
    void testUpdateCartItem_ItemNotFound() throws Exception {
        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(put("/api/cart/items/{itemId}", testItemId)
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating cart item with zero quantity
     * Verifies that zero quantity is handled
     */
    @Test
    @DisplayName("Should handle zero quantity in update request")
    void testUpdateCartItem_ZeroQuantity() throws Exception {
        UpdateCartItemRequest zeroQuantityRequest = new UpdateCartItemRequest();
        zeroQuantityRequest.setQuantity(0);

        mockMvc.perform(put("/api/cart/items/{itemId}", testItemId)
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroQuantityRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class));
    }

    /**
     * Test successfully removing cart item
     * Verifies that valid remove request returns updated cart
     */
    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() throws Exception {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(delete("/api/cart/items/{itemId}", testItemId)
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).removeCartItem(any(UUID.class), any(UUID.class));
    }

    /**
     * Test removing last item from cart
     * Verifies that removing last item returns 204 NO CONTENT
     */
    @Test
    @DisplayName("Should return 204 when removing last item from cart")
    void testRemoveCartItem_EmptyCart() throws Exception {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
                .thenReturn(null);

        mockMvc.perform(delete("/api/cart/items/{itemId}", testItemId)
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeCartItem(any(UUID.class), any(UUID.class));
    }

    /**
     * Test removing non-existent cart item
     * Verifies that service exception is properly handled
     */
    @Test
    @DisplayName("Should return error when removing non-existent item")
    void testRemoveCartItem_ItemNotFound() throws Exception {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(delete("/api/cart/items/{itemId}", testItemId)
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).removeCartItem(any(UUID.class), any(UUID.class));
    }

    /**
     * Test successfully retrieving cart
     * Verifies that cart retrieval returns cart data
     */
    @Test
    @DisplayName("Should retrieve cart successfully")
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(any(UUID.class))).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(100.00));

        verify(cartService, times(1)).getCart(any(UUID.class));
    }

    /**
     * Test retrieving empty cart
     * Verifies that empty cart is properly returned
     */
    @Test
    @DisplayName("Should return empty cart when no items exist")
    void testGetCart_EmptyCart() throws Exception {
        CartResponse emptyCart = new CartResponse();
        emptyCart.setUserId(testUserId);
        emptyCart.setItems(new ArrayList<>());
        emptyCart.setTotalPrice(BigDecimal.ZERO);
        emptyCart.setTotalItems(0);

        when(cartService.getCart(any(UUID.class))).thenReturn(emptyCart);

        mockMvc.perform(get("/api/cart")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        verify(cartService, times(1)).getCart(any(UUID.class));
    }

    /**
     * Test successfully clearing cart on logout
     * Verifies that logout clears the cart
     */
    @Test
    @DisplayName("Should clear cart successfully on logout")
    void testLogout_Success() throws Exception {
        doNothing().when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/cart/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(any(UUID.class));
    }

    /**
     * Test logout without user ID
     * Verifies that missing user ID is handled
     */
    @Test
    @DisplayName("Should return 400 when logout without user ID")
    void testLogout_MissingUserId() throws Exception {
        mockMvc.perform(post("/api/cart/logout"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).clearCart(any(UUID.class));
    }

    /**
     * Test logout with service exception
     * Verifies that service exceptions are properly handled
     */
    @Test
    @DisplayName("Should handle service exception during logout")
    void testLogout_ServiceException() throws Exception {
        doThrow(new RuntimeException("Service error")).when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/cart/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).clearCart(any(UUID.class));
    }
}