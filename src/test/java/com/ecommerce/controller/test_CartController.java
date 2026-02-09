package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for CartController
 * Tests all public endpoints for cart management operations
 * Mocks CartService layer to isolate controller logic
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
     * Test getting cart for existing user
     * Verifies that cart is retrieved successfully with lazy creation
     */
    @Test
    @DisplayName("Should successfully get cart for user with lazy creation")
    void testGetCart_Success() throws Exception {
        // Given
        Long userId = 1L;
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(1L);
        cartDTO.setUserId(userId);
        cartDTO.setTotalAmount(BigDecimal.valueOf(99.99));
        cartDTO.setItems(new ArrayList<>());
        
        when(cartService.getCart(userId)).thenReturn(cartDTO);

        // When & Then
        mockMvc.perform(get("/api/cart/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(99.99));

        verify(cartService).getCart(eq(userId));
    }

    /**
     * Test adding item to cart successfully
     * Verifies that items are added to cart with proper validation
     */
    @Test
    @DisplayName("Should successfully add item to cart")
    void testAddItemToCart_Success() throws Exception {
        // Given
        Long userId = 1L;
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setProductId(1L);
        cartItemDTO.setQuantity(2);
        cartItemDTO.setPrice(BigDecimal.valueOf(49.99));
        
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(1L);
        cartDTO.setUserId(userId);
        cartDTO.setTotalAmount(BigDecimal.valueOf(99.98));
        
        when(cartService.addItemToCart(eq(userId), any(CartItemDTO.class))).thenReturn(cartDTO);

        // When & Then
        mockMvc.perform(post("/api/cart/user/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item added to cart"))
                .andExpect(jsonPath("$.data.totalAmount").value(99.98));

        verify(cartService).addItemToCart(eq(userId), any(CartItemDTO.class));
    }

    /**
     * Test adding invalid item to cart
     * Verifies that validation errors are handled properly
     */
    @Test
    @DisplayName("Should return validation error for invalid cart item")
    void testAddItemToCart_InvalidItem() throws Exception {
        // Given
        Long userId = 1L;
        CartItemDTO invalidItem = new CartItemDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/cart/user/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test updating cart item quantity successfully
     * Verifies that item quantities can be updated
     */
    @Test
    @DisplayName("Should successfully update cart item quantity")
    void testUpdateCartItem_Success() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        Integer newQuantity = 5;
        
        CartDTO updatedCart = new CartDTO();
        updatedCart.setId(1L);
        updatedCart.setUserId(userId);
        updatedCart.setTotalAmount(BigDecimal.valueOf(249.95));
        
        when(cartService.updateCartItem(userId, itemId, newQuantity)).thenReturn(updatedCart);

        // When & Then
        mockMvc.perform(put("/api/cart/user/{userId}/items/{itemId}", userId, itemId)
                .param("quantity", newQuantity.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart item updated"))
                .andExpect(jsonPath("$.data.totalAmount").value(249.95));

        verify(cartService).updateCartItem(eq(userId), eq(itemId), eq(newQuantity));
    }

    /**
     * Test updating cart item with invalid quantity
     * Verifies that negative quantities are handled properly
     */
    @Test
    @DisplayName("Should handle update cart item with invalid quantity")
    void testUpdateCartItem_InvalidQuantity() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        Integer invalidQuantity = -1;

        // When & Then
        mockMvc.perform(put("/api/cart/user/{userId}/items/{itemId}", userId, itemId)
                .param("quantity", invalidQuantity.toString()))
                .andExpect(status().isOk()); // Controller doesn't validate, service layer should

        verify(cartService).updateCartItem(eq(userId), eq(itemId), eq(invalidQuantity));
    }

    /**
     * Test removing item from cart successfully
     * Verifies that items can be removed from cart
     */
    @Test
    @DisplayName("Should successfully remove item from cart")
    void testRemoveItemFromCart_Success() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        
        CartDTO updatedCart = new CartDTO();
        updatedCart.setId(1L);
        updatedCart.setUserId(userId);
        updatedCart.setTotalAmount(BigDecimal.ZERO);
        
        when(cartService.removeItemFromCart(userId, itemId)).thenReturn(updatedCart);

        // When & Then
        mockMvc.perform(delete("/api/cart/user/{userId}/items/{itemId}", userId, itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item removed from cart"))
                .andExpect(jsonPath("$.data.totalAmount").value(0));

        verify(cartService).removeItemFromCart(eq(userId), eq(itemId));
    }

    /**
     * Test clearing entire cart successfully
     * Verifies that all items are removed from cart
     */
    @Test
    @DisplayName("Should successfully clear entire cart")
    void testClearCart_Success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/cart/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart cleared"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(cartService).clearCart(eq(userId));
    }

    /**
     * Test cart operations with non-existent user
     * Verifies that proper error handling occurs for invalid user IDs
     */
    @Test
    @DisplayName("Should handle cart operations for non-existent user")
    void testGetCart_NonExistentUser() throws Exception {
        // Given
        Long nonExistentUserId = 999L;
        
        when(cartService.getCart(nonExistentUserId)).thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/cart/user/{userId}", nonExistentUserId))
                .andExpect(status().isInternalServerError());

        verify(cartService).getCart(eq(nonExistentUserId));
    }
}