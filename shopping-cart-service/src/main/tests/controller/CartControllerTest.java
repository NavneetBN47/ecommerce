package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.exception.*;
import com.ecommerce.shoppingcart.application.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for CartController
 * Tests all endpoints with valid, invalid, and edge case scenarios
 * Coverage: 100% of API endpoints
 */
@WebMvcTest(CartController.class)
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponse cartResponse;
    private AddCartItemRequest addCartItemRequest;
    private UpdateCartItemRequest updateCartItemRequest;

    @BeforeEach
    void setUp() {
        // Setup cart response
        CartItemResponse item1 = CartItemResponse.builder()
                .cartItemId(1L)
                .productId(101L)
                .productName("Laptop")
                .quantity(1)
                .unitPrice(new BigDecimal("999.99"))
                .subtotal(new BigDecimal("999.99"))
                .build();

        CartItemResponse item2 = CartItemResponse.builder()
                .cartItemId(2L)
                .productId(102L)
                .productName("Mouse")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("59.98"))
                .build();

        cartResponse = CartResponse.builder()
                .cartId(1L)
                .userId(1L)
                .items(Arrays.asList(item1, item2))
                .totalItems(3)
                .totalPrice(new BigDecimal("1059.97"))
                .build();

        // Setup add cart item request
        addCartItemRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(1)
                .build();

        // Setup update cart item request
        updateCartItemRequest = UpdateCartItemRequest.builder()
                .quantity(3)
                .build();
    }

    // ==================== GET /api/v1/cart/{userId} Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/cart/{userId} - Valid User ID - Should Return 200")
    void testGetCart_ValidUserId_ReturnsOk() throws Exception {
        // Given
        when(cartService.getCart(anyLong())).thenReturn(cartResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/cart/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPrice").value(1059.97));

        verify(cartService, times(1)).getCart(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/cart/{userId} - Empty Cart - Should Return 200")
    void testGetCart_EmptyCart_ReturnsOk() throws Exception {
        // Given
        CartResponse emptyCart = CartResponse.builder()
                .cartId(1L)
                .userId(1L)
                .items(Collections.emptyList())
                .totalItems(0)
                .totalPrice(BigDecimal.ZERO)
                .build();

        when(cartService.getCart(anyLong())).thenReturn(emptyCart);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/cart/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        verify(cartService, times(1)).getCart(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/cart/{userId} - Cart Not Found - Should Return 404")
    void testGetCart_CartNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(cartService.getCart(anyLong()))
                .thenThrow(new CartNotFoundException("Cart not found for user ID: 999"));

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/cart/999")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).getCart(999L);
    }

    @Test
    @DisplayName("GET /api/v1/cart/{userId} - No Authentication - Should Return 401")
    void testGetCart_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/cart/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart(anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/cart/{userId} - Invalid User ID Format - Should Return 400")
    void testGetCart_InvalidUserIdFormat_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/cart/invalid")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, never()).getCart(anyLong());
    }

    // ==================== POST /api/v1/cart/{userId}/items Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/cart/{userId}/items - Valid Request - Should Return 200")
    void testAddItemToCart_ValidRequest_ReturnsOk() throws Exception {
        // Given
        when(cartService.addItemToCart(anyLong(), any(AddCartItemRequest.class)))
                .thenReturn(cartResponse);

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/cart/1/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCartItemRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/cart/{userId}/items - Product Not Found - Should Return 404")
    void testAddItemToCart_ProductNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(cartService.addItemToCart(anyLong(), any(AddCartItemRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/cart/1/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCartItemRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/cart/{userId}/items - Insufficient Stock - Should Return 400")
    void testAddItemToCart_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Given
        when(cartService.addItemToCart(anyLong(), any(AddCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/cart/1/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCartItemRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/cart/{userId}/items - Invalid Quantity - Should Return 400")
    void testAddItemToCart_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Given
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(0)
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/cart/1/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/cart/{userId}/items - Missing Product ID - Should Return 400")
    void testAddItemToCart_MissingProductId_ReturnsBadRequest() throws Exception {
        // Given
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .quantity(1)
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/cart/1/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    // ==================== PUT /api/v1/cart/{userId}/items/{cartItemId} Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("PUT /api/v1/cart/{userId}/items/{cartItemId} - Valid Update - Should Return 200")
    void testUpdateCartItem_ValidRequest_ReturnsOk() throws Exception {
        // Given
        when(cartService.updateCartItem(anyLong(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        // When
        ResultActions result = mockMvc.perform(put("/api/v1/cart/1/items/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCartItemRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("PUT /api/v1/cart/{userId}/items/{cartItemId} - Cart Item Not Found - Should Return 404")
    void testUpdateCartItem_CartItemNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(cartService.updateCartItem(anyLong(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        // When
        ResultActions result = mockMvc.perform(put("/api/v1/cart/1/items/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCartItemRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(999L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("PUT /api/v1/cart/{userId}/items/{cartItemId} - Invalid Quantity - Should Return 400")
    void testUpdateCartItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Given
        UpdateCartItemRequest invalidRequest = UpdateCartItemRequest.builder()
                .quantity(-1)
                .build();

        // When
        ResultActions result = mockMvc.perform(put("/api/v1/cart/1/items/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(anyLong(), anyLong(), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("PUT /api/v1/cart/{userId}/items/{cartItemId} - Insufficient Stock - Should Return 400")
    void testUpdateCartItem_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Given
        when(cartService.updateCartItem(anyLong(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        // When
        ResultActions result = mockMvc.perform(put("/api/v1/cart/1/items/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCartItemRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class));
    }

    // ==================== DELETE /api/v1/cart/{userId}/items/{cartItemId} Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("DELETE /api/v1/cart/{userId}/items/{cartItemId} - Valid Request - Should Return 200")
    void testRemoveItemFromCart_ValidRequest_ReturnsOk() throws Exception {
        // Given
        when(cartService.removeItemFromCart(anyLong(), anyLong()))
                .thenReturn(cartResponse);

        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/1/items/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));

        verify(cartService, times(1)).removeItemFromCart(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("DELETE /api/v1/cart/{userId}/items/{cartItemId} - Cart Item Not Found - Should Return 404")
    void testRemoveItemFromCart_CartItemNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(cartService.removeItemFromCart(anyLong(), anyLong()))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/1/items/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeItemFromCart(1L, 999L);
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/{userId}/items/{cartItemId} - No Authentication - Should Return 401")
    void testRemoveItemFromCart_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/1/items/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).removeItemFromCart(anyLong(), anyLong());
    }

    // ==================== DELETE /api/v1/cart/{userId} Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("DELETE /api/v1/cart/{userId} - Valid Request - Should Return 204")
    void testClearCart_ValidRequest_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(cartService).clearCart(anyLong());

        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("DELETE /api/v1/cart/{userId} - Cart Not Found - Should Return 404")
    void testClearCart_CartNotFound_ReturnsNotFound() throws Exception {
        // Given
        doThrow(new CartNotFoundException("Cart not found"))
                .when(cartService).clearCart(anyLong());

        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).clearCart(999L);
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/{userId} - No Authentication - Should Return 401")
    void testClearCart_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(anyLong());
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = {"USER"})
    @DisplayName("DELETE /api/v1/cart/{userId} - Forbidden Access - Should Return 403")
    void testClearCart_ForbiddenAccess_ReturnsForbidden() throws Exception {
        // Given
        doThrow(new ForbiddenException("Access denied"))
                .when(cartService).clearCart(anyLong());

        // When
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isForbidden());

        verify(cartService, times(1)).clearCart(2L);
    }
}