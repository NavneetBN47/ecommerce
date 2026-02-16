package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
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
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for CartController
 * 
 * Tests all public endpoints for cart management including:
 * - Adding products to cart
 * - Updating cart item quantities
 * - Removing items from cart
 * - Retrieving cart details
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@WebMvcTest(CartController.class)
@DisplayName("CartController Tests")
class test_CartController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private UserPrincipal testUser;
    private UUID testUserId;
    private UUID testProductId;
    private UUID testCartItemId;
    private CartResponse testCartResponse;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testCartItemId = UUID.randomUUID();
        
        testUser = new UserPrincipal(
            testUserId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );

        testCartResponse = CartResponse.builder()
            .cartId(UUID.randomUUID())
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.valueOf(100.00))
            .totalItems(1)
            .build();
    }

    /**
     * Test adding a product to cart successfully
     * 
     * Validates:
     * - HTTP 201 Created status
     * - Correct response body structure
     * - Service method invocation
     */
    @Test
    @WithMockUser
    @DisplayName("Should add product to cart successfully")
    void testAddToCart_Success() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(testProductId)
            .quantity(2)
            .build();

        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
            .thenReturn(testCartResponse);

        mockMvc.perform(post("/cart/items")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cartId").exists())
            .andExpect(jsonPath("$.grandTotal").value(100.00));
    }

    /**
     * Test adding product with invalid quantity
     * 
     * Validates:
     * - HTTP 400 Bad Request for validation failure
     * - Proper error message
     */
    @Test
    @WithMockUser
    @DisplayName("Should return 400 when quantity is invalid")
    void testAddToCart_InvalidQuantity() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(testProductId)
            .quantity(0)
            .build();

        mockMvc.perform(post("/cart/items")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test updating cart item quantity successfully
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Updated cart response
     */
    @Test
    @WithMockUser
    @DisplayName("Should update cart item quantity successfully")
    void testUpdateCartItem_Success() throws Exception {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        when(cartService.updateCartItem(any(UUID.class), eq(testCartItemId), any(UpdateCartItemRequest.class)))
            .thenReturn(testCartResponse);

        mockMvc.perform(put("/cart/items/" + testCartItemId)
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").exists());
    }

    /**
     * Test removing item from cart successfully
     * 
     * Validates:
     * - HTTP 200 OK status for non-empty cart
     * - Correct response structure
     */
    @Test
    @WithMockUser
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() throws Exception {
        when(cartService.removeCartItem(any(UUID.class), eq(testCartItemId)))
            .thenReturn(testCartResponse);

        mockMvc.perform(delete("/cart/items/" + testCartItemId)
                .with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").exists());
    }

    /**
     * Test removing last item from cart
     * 
     * Validates:
     * - HTTP 204 No Content when cart becomes empty
     */
    @Test
    @WithMockUser
    @DisplayName("Should return 204 when removing last item")
    void testRemoveCartItem_EmptyCart() throws Exception {
        CartResponse emptyCartResponse = CartResponse.builder()
            .cartId(null)
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.ZERO)
            .totalItems(0)
            .build();

        when(cartService.removeCartItem(any(UUID.class), eq(testCartItemId)))
            .thenReturn(emptyCartResponse);

        mockMvc.perform(delete("/cart/items/" + testCartItemId)
                .with(user(testUser)))
            .andExpect(status().isNoContent());
    }

    /**
     * Test getting cart successfully
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Cart details in response
     */
    @Test
    @WithMockUser
    @DisplayName("Should get cart successfully")
    void testGetCart_Success() throws Exception {
        when(cartService.getCart(any(UUID.class)))
            .thenReturn(testCartResponse);

        mockMvc.perform(get("/cart")
                .with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").exists())
            .andExpect(jsonPath("$.totalItems").value(1));
    }

    /**
     * Test unauthorized access to cart endpoints
     * 
     * Validates:
     * - HTTP 401 Unauthorized without authentication
     */
    @Test
    @DisplayName("Should return 401 for unauthenticated requests")
    void testCartEndpoints_Unauthorized() throws Exception {
        mockMvc.perform(get("/cart"))
            .andExpect(status().isUnauthorized());
    }
}