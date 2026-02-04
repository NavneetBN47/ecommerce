package com.example.ecommerce.controller;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for CartController.
 * Tests all REST endpoints for cart operations including authentication and validation.
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponse cartResponse;
    private CartItemResponse cartItemResponse;
    private AddToCartRequest addToCartRequest;
    private UpdateCartItemRequest updateCartItemRequest;
    private UUID userId;
    private UUID productId;
    private UUID cartItemId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

        cartItemResponse = new CartItemResponse(
                cartItemId,
                productId,
                "Test Product",
                BigDecimal.valueOf(99.99),
                2,
                BigDecimal.valueOf(199.98)
        );

        cartResponse = new CartResponse(
                UUID.randomUUID(),
                userId,
                LocalDateTime.now(),
                Arrays.asList(cartItemResponse),
                BigDecimal.valueOf(199.98),
                1
        );

        addToCartRequest = new AddToCartRequest(productId, 2);
        updateCartItemRequest = new UpdateCartItemRequest(3);
    }

    /**
     * Test successful retrieval of user's cart.
     */
    @Test
    void getCart_ShouldReturnCartResponse() throws Exception {
        // Given
        when(cartService.getCart(any(UUID.class))).thenReturn(cartResponse);

        // When & Then
        mockMvc.perform(get("/api/cart")
                        .requestAttr("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartId").value(cartResponse.getCartId().toString()))
                .andExpect(jsonPath("$.grandTotal").value(199.98))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(cartItemId.toString()))
                .andExpect(jsonPath("$.items[0].productName").value("Test Product"));

        verify(cartService).getCart(userId);
    }

    /**
     * Test successful addition of item to cart.
     */
    @Test
    void addProductToCart_ValidRequest_ShouldReturnCartResponse() throws Exception {
        // Given
        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
                .thenReturn(cartResponse);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartId").value(cartResponse.getCartId().toString()))
                .andExpect(jsonPath("$.grandTotal").value(199.98));

        verify(cartService).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test add to cart with invalid request body.
     */
    @Test
    void addProductToCart_InvalidRequest_ShouldReturn400() throws Exception {
        // Given
        AddToCartRequest invalidRequest = new AddToCartRequest(null, 0);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    /**
     * Test successful update of cart item.
     */
    @Test
    void updateCartItemQuantity_ValidRequest_ShouldReturnUpdatedCartResponse() throws Exception {
        // Given
        when(cartService.updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        // When & Then
        mockMvc.perform(put("/api/cart/items/{id}", cartItemId)
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartId").value(cartResponse.getCartId().toString()));

        verify(cartService).updateCartItemQuantity(eq(userId), eq(cartItemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test update cart item with invalid quantity.
     */
    @Test
    void updateCartItemQuantity_InvalidQuantity_ShouldReturn400() throws Exception {
        // Given
        UpdateCartItemRequest invalidRequest = new UpdateCartItemRequest(-1);

        // When & Then
        mockMvc.perform(put("/api/cart/items/{id}", cartItemId)
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    /**
     * Test successful removal of cart item.
     */
    @Test
    void removeCartItem_ValidId_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(cartService).removeCartItem(any(UUID.class), eq(cartItemId));

        // When & Then
        mockMvc.perform(delete("/api/cart/items/{id}", cartItemId)
                        .requestAttr("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(cartService).removeCartItem(eq(userId), eq(cartItemId));
    }

    /**
     * Test successful cart logout (clear cart).
     */
    @Test
    void clearCartOnLogout_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(cartService).clearCartOnLogout(any(UUID.class));

        // When & Then
        mockMvc.perform(post("/api/cart/logout")
                        .requestAttr("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(cartService).clearCartOnLogout(eq(userId));
    }

    /**
     * Test malformed JSON handling.
     */
    @Test
    void addProductToCart_MalformedJson_ShouldReturn400() throws Exception {
        // Given
        String malformedJson = "{\"productId\": \"invalid\", \"quantity\":}";

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }
}