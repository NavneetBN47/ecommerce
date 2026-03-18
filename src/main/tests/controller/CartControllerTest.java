package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.dto.*;
import com.ecommerce.shoppingcart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    private CartDTO cartDTO;
    private CartItemDTO cartItemDTO;
    private AddCartItemDTO addCartItemDTO;
    private UpdateCartItemDTO updateCartItemDTO;

    @BeforeEach
    void setUp() {
        cartItemDTO = CartItemDTO.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Laptop")
                .price(new BigDecimal("999.99"))
                .quantity(2)
                .subtotal(new BigDecimal("1999.98"))
                .build();

        cartDTO = CartDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .items(Arrays.asList(cartItemDTO))
                .totalItems(2)
                .totalPrice(new BigDecimal("1999.98"))
                .build();

        addCartItemDTO = AddCartItemDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        updateCartItemDTO = UpdateCartItemDTO.builder()
                .quantity(3)
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("Should get cart successfully when authenticated")
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(any(UUID.class))).thenReturn(cartDTO);

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(1999.98));

        verify(cartService, times(1)).getCart(any(UUID.class));
    }

    @Test
    @DisplayName("Should return 401 when getting cart without authentication")
    void testGetCart_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart(any(UUID.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return empty cart when no items")
    void testGetCart_EmptyCart() throws Exception {
        CartDTO emptyCart = CartDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .items(Collections.emptyList())
                .totalItems(0)
                .totalPrice(BigDecimal.ZERO)
                .build();

        when(cartService.getCart(any(UUID.class))).thenReturn(emptyCart);

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        verify(cartService, times(1)).getCart(any(UUID.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should add item to cart successfully")
    void testAddItemToCart_Success() throws Exception {
        when(cartService.addItemToCart(any(UUID.class), any(AddCartItemDTO.class)))
                .thenReturn(cartDTO);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(2));

        verify(cartService, times(1)).addItemToCart(any(UUID.class), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when adding item with invalid quantity")
    void testAddItemToCart_InvalidQuantity() throws Exception {
        AddCartItemDTO invalidDTO = AddCartItemDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when adding non-existent product")
    void testAddItemToCart_ProductNotFound() throws Exception {
        when(cartService.addItemToCart(any(UUID.class), any(AddCartItemDTO.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).addItemToCart(any(UUID.class), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when adding item with insufficient stock")
    void testAddItemToCart_InsufficientStock() throws Exception {
        when(cartService.addItemToCart(any(UUID.class), any(AddCartItemDTO.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addItemToCart(any(UUID.class), any(AddCartItemDTO.class));
    }

    @Test
    @DisplayName("Should return 401 when adding item without authentication")
    void testAddItemToCart_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should update cart item successfully")
    void testUpdateCartItem_Success() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(cartService.updateCartItem(any(UUID.class), eq(itemId), any(UpdateCartItemDTO.class)))
                .thenReturn(cartDTO);

        mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).updateCartItem(any(UUID.class), eq(itemId), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when updating non-existent cart item")
    void testUpdateCartItem_NotFound() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(cartService.updateCartItem(any(UUID.class), eq(itemId), any(UpdateCartItemDTO.class)))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartItemDTO)))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).updateCartItem(any(UUID.class), eq(itemId), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when updating with invalid quantity")
    void testUpdateCartItem_InvalidQuantity() throws Exception {
        UUID itemId = UUID.randomUUID();
        UpdateCartItemDTO invalidDTO = UpdateCartItemDTO.builder()
                .quantity(100)
                .build();

        when(cartService.updateCartItem(any(UUID.class), eq(itemId), any(UpdateCartItemDTO.class)))
                .thenThrow(new InvalidQuantityException("Quantity exceeds maximum"));

        mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).updateCartItem(any(UUID.class), eq(itemId), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(cartService.removeCartItem(any(UUID.class), eq(itemId))).thenReturn(cartDTO);

        mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).removeCartItem(any(UUID.class), eq(itemId));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when removing non-existent cart item")
    void testRemoveCartItem_NotFound() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(cartService.removeCartItem(any(UUID.class), eq(itemId)))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeCartItem(any(UUID.class), eq(itemId));
    }

    @Test
    @WithMockUser
    @DisplayName("Should clear cart successfully")
    void testClearCart_Success() throws Exception {
        doNothing().when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(any(UUID.class));
    }

    @Test
    @DisplayName("Should return 401 when clearing cart without authentication")
    void testClearCart_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(any(UUID.class));
    }
}