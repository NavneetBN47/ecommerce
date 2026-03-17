package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.dto.*;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Controller Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartResponse cartResponse;
    private AddCartItemRequest addItemRequest;
    private UpdateCartItemRequest updateItemRequest;

    @BeforeEach
    void setUp() {
        CartItemResponse item1 = CartItemResponse.builder()
                .cartItemId(1L)
                .productId(101L)
                .productName("Product 1")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .subtotal(new BigDecimal("199.98"))
                .build();

        CartItemResponse item2 = CartItemResponse.builder()
                .cartItemId(2L)
                .productId(102L)
                .productName("Product 2")
                .quantity(1)
                .unitPrice(new BigDecimal("49.99"))
                .subtotal(new BigDecimal("49.99"))
                .build();

        List<CartItemResponse> items = Arrays.asList(item1, item2);

        cartResponse = CartResponse.builder()
                .cartId(1L)
                .userId(1L)
                .items(items)
                .totalItems(3)
                .totalPrice(new BigDecimal("249.97"))
                .build();

        addItemRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(2)
                .build();

        updateItemRequest = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("GET /api/v1/cart - Authenticated User - Success")
    void testGetCart_AuthenticatedUser_ReturnsCart() throws Exception {
        when(cartService.getCartByUserId(1L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPrice").value(249.97));

        verify(cartService, times(1)).getCartByUserId(1L);
    }

    @Test
    @DisplayName("GET /api/v1/cart - Unauthenticated User - Returns Unauthorized")
    void testGetCart_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCartByUserId(anyLong());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("GET /api/v1/cart - Empty Cart - Returns Empty Cart")
    void testGetCart_EmptyCart_ReturnsEmptyCart() throws Exception {
        CartResponse emptyCart = CartResponse.builder()
                .cartId(1L)
                .userId(1L)
                .items(Arrays.asList())
                .totalItems(0)
                .totalPrice(BigDecimal.ZERO)
                .build();

        when(cartService.getCartByUserId(1L)).thenReturn(emptyCart);

        mockMvc.perform(get("/api/v1/cart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        verify(cartService, times(1)).getCartByUserId(1L);
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("POST /api/v1/cart/items - Valid Request - Success")
    void testAddItemToCart_ValidRequest_ReturnsUpdatedCart() throws Exception {
        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(3));

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("POST /api/v1/cart/items - Invalid Product ID - Returns Bad Request")
    void testAddItemToCart_InvalidProductId_ReturnsBadRequest() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId(0L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("POST /api/v1/cart/items - Zero Quantity - Returns Bad Request")
    void testAddItemToCart_ZeroQuantity_ReturnsBadRequest() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("POST /api/v1/cart/items - Negative Quantity - Returns Bad Request")
    void testAddItemToCart_NegativeQuantity_ReturnsBadRequest() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(-5)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Unauthenticated User - Returns Unauthorized")
    void testAddItemToCart_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("PUT /api/v1/cart/items/{cartItemId} - Valid Request - Success")
    void testUpdateCartItem_ValidRequest_ReturnsUpdatedCart() throws Exception {
        when(cartService.updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(put("/api/v1/cart/items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("PUT /api/v1/cart/items/{cartItemId} - Invalid Quantity - Returns Bad Request")
    void testUpdateCartItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        UpdateCartItemRequest invalidRequest = UpdateCartItemRequest.builder()
                .quantity(0)
                .build();

        mockMvc.perform(put("/api/v1/cart/items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(anyLong(), anyLong(), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "2")
    @DisplayName("PUT /api/v1/cart/items/{cartItemId} - Different User - Returns Forbidden")
    void testUpdateCartItem_DifferentUser_ReturnsForbidden() throws Exception {
        when(cartService.updateCartItem(eq(2L), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new ForbiddenException("User does not own this cart item"));

        mockMvc.perform(put("/api/v1/cart/items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isForbidden());

        verify(cartService, times(1)).updateCartItem(eq(2L), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("DELETE /api/v1/cart/items/{cartItemId} - Valid Request - Success")
    void testRemoveItemFromCart_ValidRequest_ReturnsUpdatedCart() throws Exception {
        when(cartService.removeItemFromCart(1L, 1L)).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/v1/cart/items/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).removeItemFromCart(1L, 1L);
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("DELETE /api/v1/cart/items/{cartItemId} - Item Not Found - Returns Not Found")
    void testRemoveItemFromCart_ItemNotFound_ReturnsNotFound() throws Exception {
        when(cartService.removeItemFromCart(1L, 999L))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        mockMvc.perform(delete("/api/v1/cart/items/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeItemFromCart(1L, 999L);
    }

    @Test
    @WithMockUser(username = "2")
    @DisplayName("DELETE /api/v1/cart/items/{cartItemId} - Different User - Returns Forbidden")
    void testRemoveItemFromCart_DifferentUser_ReturnsForbidden() throws Exception {
        when(cartService.removeItemFromCart(2L, 1L))
                .thenThrow(new ForbiddenException("User does not own this cart item"));

        mockMvc.perform(delete("/api/v1/cart/items/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cartService, times(1)).removeItemFromCart(2L, 1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{cartItemId} - Unauthenticated User - Returns Unauthorized")
    void testRemoveItemFromCart_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).removeItemFromCart(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("DELETE /api/v1/cart - Valid Request - Success")
    void testClearCart_ValidRequest_ReturnsNoContent() throws Exception {
        doNothing().when(cartService).clearCart(1L);

        mockMvc.perform(delete("/api/v1/cart")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("DELETE /api/v1/cart - Cart Not Found - Returns Not Found")
    void testClearCart_CartNotFound_ReturnsNotFound() throws Exception {
        doThrow(new CartNotFoundException("Cart not found"))
                .when(cartService).clearCart(1L);

        mockMvc.perform(delete("/api/v1/cart")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/cart - Unauthenticated User - Returns Unauthorized")
    void testClearCart_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(anyLong());
    }
}