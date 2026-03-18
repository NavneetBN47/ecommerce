package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.dto.AddToCartRequest;
import com.ecommerce.shoppingcart.dto.CartResponse;
import com.ecommerce.shoppingcart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private AddToCartRequest addToCartRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setUserId(1L);
        addToCartRequest.setProductId(1L);
        addToCartRequest.setQuantity(2);

        cartResponse = new CartResponse();
        cartResponse.setId(1L);
        cartResponse.setUserId(1L);
        cartResponse.setItems(new ArrayList<>());
        cartResponse.setTotalAmount(new BigDecimal("199.98"));
    }

    @Test
    @DisplayName("POST /api/cart/add - Add Item to Cart Success")
    @WithMockUser
    void testAddToCart_Success() throws Exception {
        when(cartService.addToCart(any(AddToCartRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(199.98));
    }

    @Test
    @DisplayName("POST /api/cart/add - Invalid Quantity")
    @WithMockUser
    void testAddToCart_InvalidQuantity() throws Exception {
        addToCartRequest.setQuantity(0);

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cart/add - Missing Required Fields")
    @WithMockUser
    void testAddToCart_MissingFields() throws Exception {
        addToCartRequest.setProductId(null);

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cart/{userId} - Get Cart Success")
    @WithMockUser
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(anyLong())).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("GET /api/cart/{userId} - Cart Not Found")
    @WithMockUser
    void testGetCart_NotFound() throws Exception {
        when(cartService.getCart(anyLong()))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/api/cart/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/cart/update - Update Cart Item Success")
    @WithMockUser
    void testUpdateCartItem_Success() throws Exception {
        when(cartService.updateCartItem(anyLong(), anyLong(), anyInt())).thenReturn(cartResponse);

        mockMvc.perform(put("/api/cart/update")
                .param("userId", "1")
                .param("productId", "1")
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("DELETE /api/cart/remove - Remove Item Success")
    @WithMockUser
    void testRemoveFromCart_Success() throws Exception {
        when(cartService.removeFromCart(anyLong(), anyLong())).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/cart/remove")
                .param("userId", "1")
                .param("productId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/cart/clear/{userId} - Clear Cart Success")
    @WithMockUser
    void testClearCart_Success() throws Exception {
        mockMvc.perform(delete("/api/cart/clear/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/cart/add - Unauthorized Access")
    void testAddToCart_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isUnauthorized());
    }
}
