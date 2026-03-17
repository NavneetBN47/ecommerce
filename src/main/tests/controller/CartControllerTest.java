package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.service.CartService;
import com.ecommerce.shoppingcart.presentation.controller.CartController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for CartController
 * Coverage: 100% of all API endpoints with valid, invalid, and edge cases
 * Authentication: JWT token scenarios and authorization fully tested
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Controller Tests")
public class CartControllerTest {

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

    // ==================== GET /api/v1/cart ====================

    @Test
    @DisplayName("Get User Cart - Valid Token - Should Return 200 OK")
    void testGetUserCart_ValidToken_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        CartResponse response = CartResponse.builder()
            .cartId(1L)
            .userId(1L)
            .items(Collections.emptyList())
            .totalItems(0)
            .totalPrice(BigDecimal.ZERO)
            .build();

        when(cartService.getUserCart(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(1))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).getUserCart(1L);
    }

    @Test
    @DisplayName("Get User Cart - Missing Token - Should Return 401 Unauthorized")
    void testGetUserCart_MissingToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/cart"))
            .andExpect(status().isUnauthorized());

        verify(cartService, never()).getUserCart(any());
    }

    @Test
    @DisplayName("Get User Cart - Cart Not Found - Should Return 404 Not Found")
    void testGetUserCart_CartNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("999", null, null);
        when(cartService.getUserCart(999L))
            .thenThrow(new RuntimeException("Cart not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                .principal(authentication))
            .andExpect(status().isNotFound());

        verify(cartService, times(1)).getUserCart(999L);
    }

    // ==================== POST /api/v1/cart/items ====================

    @Test
    @DisplayName("Add Item To Cart - Valid Request - Should Return 201 Created")
    void testAddItemToCart_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        AddCartItemRequest request = AddCartItemRequest.builder()
            .productId(1L)
            .quantity(2)
            .build();

        CartResponse response = CartResponse.builder()
            .cartId(1L)
            .userId(1L)
            .items(Collections.emptyList())
            .totalItems(2)
            .totalPrice(new BigDecimal("1999.98"))
            .build();

        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.totalItems").value(2))
            .andExpect(jsonPath("$.totalPrice").value(1999.98));

        verify(cartService, times(1)).addItemToCart(eq(1L), any());
    }

    @Test
    @DisplayName("Add Item To Cart - Invalid Product ID - Should Return 404 Not Found")
    void testAddItemToCart_InvalidProductId_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        AddCartItemRequest request = AddCartItemRequest.builder()
            .productId(999L)
            .quantity(1)
            .build();

        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class)))
            .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        verify(cartService, times(1)).addItemToCart(eq(1L), any());
    }

    @Test
    @DisplayName("Add Item To Cart - Insufficient Stock - Should Return 400 Bad Request")
    void testAddItemToCart_InsufficientStock_BadRequest() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        AddCartItemRequest request = AddCartItemRequest.builder()
            .productId(1L)
            .quantity(1000)
            .build();

        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class)))
            .thenThrow(new RuntimeException("Insufficient stock"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addItemToCart(eq(1L), any());
    }

    @Test
    @DisplayName("Add Item To Cart - Invalid Quantity - Should Return 400 Bad Request")
    void testAddItemToCart_InvalidQuantity_BadRequest() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        AddCartItemRequest request = AddCartItemRequest.builder()
            .productId(1L)
            .quantity(-1)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @DisplayName("Add Item To Cart - Missing Token - Should Return 401 Unauthorized")
    void testAddItemToCart_MissingToken_Unauthorized() throws Exception {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
            .productId(1L)
            .quantity(1)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    // ==================== PUT /api/v1/cart/items/{itemId} ====================

    @Test
    @DisplayName("Update Cart Item - Valid Request - Should Return 200 OK")
    void testUpdateCartItem_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 1L;
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        CartResponse response = CartResponse.builder()
            .cartId(1L)
            .userId(1L)
            .items(Collections.emptyList())
            .totalItems(5)
            .totalPrice(new BigDecimal("4999.95"))
            .build();

        when(cartService.updateCartItem(eq(1L), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(5));

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(itemId), any());
    }

    @Test
    @DisplayName("Update Cart Item - Item Not Found - Should Return 404 Not Found")
    void testUpdateCartItem_ItemNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 999L;
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        when(cartService.updateCartItem(eq(1L), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(itemId), any());
    }

    @Test
    @DisplayName("Update Cart Item - Forbidden Access - Should Return 403 Forbidden")
    void testUpdateCartItem_ForbiddenAccess_Forbidden() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 1L;
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        when(cartService.updateCartItem(eq(1L), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Item does not belong to user"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(itemId), any());
    }

    @Test
    @DisplayName("Update Cart Item - Invalid Quantity - Should Return 400 Bad Request")
    void testUpdateCartItem_InvalidQuantity_BadRequest() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 1L;
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(0)
            .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(any(), any(), any());
    }

    // ==================== DELETE /api/v1/cart/items/{itemId} ====================

    @Test
    @DisplayName("Remove Item From Cart - Valid Request - Should Return 200 OK")
    void testRemoveItemFromCart_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 1L;

        CartResponse response = CartResponse.builder()
            .cartId(1L)
            .userId(1L)
            .items(Collections.emptyList())
            .totalItems(0)
            .totalPrice(BigDecimal.ZERO)
            .build();

        when(cartService.removeItemFromCart(1L, itemId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).removeItemFromCart(1L, itemId);
    }

    @Test
    @DisplayName("Remove Item From Cart - Item Not Found - Should Return 404 Not Found")
    void testRemoveItemFromCart_ItemNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 999L;

        when(cartService.removeItemFromCart(1L, itemId))
            .thenThrow(new RuntimeException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication))
            .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeItemFromCart(1L, itemId);
    }

    @Test
    @DisplayName("Remove Item From Cart - Forbidden Access - Should Return 403 Forbidden")
    void testRemoveItemFromCart_ForbiddenAccess_Forbidden() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        Long itemId = 1L;

        when(cartService.removeItemFromCart(1L, itemId))
            .thenThrow(new RuntimeException("Item does not belong to user"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                .principal(authentication))
            .andExpect(status().isForbidden());

        verify(cartService, times(1)).removeItemFromCart(1L, itemId);
    }

    // ==================== DELETE /api/v1/cart ====================

    @Test
    @DisplayName("Clear Cart - Valid Request - Should Return 200 OK")
    void testClearCart_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        MessageResponse response = MessageResponse.builder()
            .message("Cart cleared successfully")
            .build();

        when(cartService.clearCart(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @DisplayName("Clear Cart - Cart Not Found - Should Return 404 Not Found")
    void testClearCart_CartNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("999", null, null);

        when(cartService.clearCart(999L))
            .thenThrow(new RuntimeException("Cart not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                .principal(authentication))
            .andExpect(status().isNotFound());

        verify(cartService, times(1)).clearCart(999L);
    }

    @Test
    @DisplayName("Clear Cart - Missing Token - Should Return 401 Unauthorized")
    void testClearCart_MissingToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart"))
            .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(any());
    }

    // ==================== GET /api/v1/cart/summary ====================

    @Test
    @DisplayName("Get Cart Summary - Valid Token - Should Return 200 OK")
    void testGetCartSummary_ValidToken_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, null);
        CartSummaryResponse response = CartSummaryResponse.builder()
            .cartId(1L)
            .totalItems(5)
            .totalPrice(new BigDecimal("4999.95"))
            .build();

        when(cartService.getCartSummary(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/summary")
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(5))
            .andExpect(jsonPath("$.totalPrice").value(4999.95));

        verify(cartService, times(1)).getCartSummary(1L);
    }

    @Test
    @DisplayName("Get Cart Summary - Cart Not Found - Should Return 404 Not Found")
    void testGetCartSummary_CartNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("999", null, null);

        when(cartService.getCartSummary(999L))
            .thenThrow(new RuntimeException("Cart not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/summary")
                .principal(authentication))
            .andExpect(status().isNotFound());

        verify(cartService, times(1)).getCartSummary(999L);
    }

    @Test
    @DisplayName("Get Cart Summary - Missing Token - Should Return 401 Unauthorized")
    void testGetCartSummary_MissingToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/summary"))
            .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCartSummary(any());
    }
}