package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.service.CartService;
import com.ecommerce.shoppingcart.application.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
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
        cartResponse = new CartResponse();
        cartResponse.setCartId(1L);
        cartResponse.setUserId(1L);
        cartResponse.setItems(Arrays.asList());
        cartResponse.setTotalAmount(new BigDecimal("0.00"));
        cartResponse.setItemCount(0);

        addItemRequest = new AddCartItemRequest();
        addItemRequest.setProductId(1L);
        addItemRequest.setQuantity(2);

        updateItemRequest = new UpdateCartItemRequest();
        updateItemRequest.setQuantity(3);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/cart - Success")
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(anyString())).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(0.00));

        verify(cartService, times(1)).getCart("test@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/cart - Unauthorized")
    void testGetCart_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart(anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/cart - Cart Not Found (Auto-Create)")
    void testGetCart_NotFound() throws Exception {
        when(cartService.getCart(anyString()))
                .thenThrow(new CartNotFoundException("Cart not found"));

        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Success")
    void testAddItemToCart_Success() throws Exception {
        CartItemResponse itemResponse = new CartItemResponse();
        itemResponse.setItemId(1L);
        itemResponse.setProductId(1L);
        itemResponse.setQuantity(2);
        itemResponse.setPrice(new BigDecimal("99.99"));
        itemResponse.setSubtotal(new BigDecimal("199.98"));

        cartResponse.setItems(Arrays.asList(itemResponse));
        cartResponse.setTotalAmount(new BigDecimal("199.98"));
        cartResponse.setItemCount(1);

        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(199.98));

        verify(cartService, times(1)).addItemToCart(anyString(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Product Not Found")
    void testAddItemToCart_ProductNotFound() throws Exception {
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Insufficient Stock")
    void testAddItemToCart_InsufficientStock() throws Exception {
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Invalid Quantity")
    void testAddItemToCart_InvalidQuantity() throws Exception {
        addItemRequest.setQuantity(0);

        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new InvalidQuantityException("Quantity must be greater than 0"));

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Missing Required Fields")
    void testAddItemToCart_MissingFields() throws Exception {
        addItemRequest.setProductId(null);

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Success")
    void testUpdateCartItem_Success() throws Exception {
        when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Item Not Found")
    void testUpdateCartItem_ItemNotFound() throws Exception {
        when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", 999L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Insufficient Stock")
    void testUpdateCartItem_InsufficientStock() throws Exception {
        updateItemRequest.setQuantity(1000);

        when(cartService.updateCartItem(anyString(), anyLong(), any(UpdateCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for requested quantity"));

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Success")
    void testRemoveItemFromCart_Success() throws Exception {
        when(cartService.removeItemFromCart(anyString(), anyLong())).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", 1L)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).removeItemFromCart("test@example.com", 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Item Not Found")
    void testRemoveItemFromCart_ItemNotFound() throws Exception {
        when(cartService.removeItemFromCart(anyString(), anyLong()))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart - Success")
    void testClearCart_Success() throws Exception {
        MessageResponse response = new MessageResponse("Cart cleared successfully");

        when(cartService.clearCart(anyString())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/cart")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        verify(cartService, times(1)).clearCart("test@example.com");
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    @DisplayName("Authorization - User Can Only Access Own Cart")
    void testAuthorization_OwnCartOnly() throws Exception {
        when(cartService.getCart(anyString())).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).getCart("user1@example.com");
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    @DisplayName("Authorization - Cannot Access Another User's Cart")
    void testAuthorization_CannotAccessOtherCart() throws Exception {
        when(cartService.removeItemFromCart(anyString(), anyLong()))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", 1L)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Concurrent Modification - Optimistic Locking")
    void testConcurrentModification() throws Exception {
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Optimistic locking failure"));

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isInternalServerError());
    }
}