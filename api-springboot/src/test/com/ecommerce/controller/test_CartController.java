package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.UserPrincipal;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for CartController
 * 
 * This test class verifies the REST API endpoints for shopping cart management.
 * It tests all public methods including:
 * - Adding products to cart
 * - Updating cart item quantities
 * - Removing items from cart
 * - Retrieving cart details
 * 
 * @author Test Generation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Test Suite")
class test_CartController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserPrincipal userPrincipal;
    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private UUID itemId;

    /**
     * Setup method executed before each test
     * Initializes test data and MockMvc instance
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
        
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        
        userPrincipal = new UserPrincipal(
            userId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );
    }

    /**
     * Test successful addition of product to cart
     * 
     * Verifies that:
     * - HTTP 201 Created status is returned
     * - Cart response contains correct data
     * - Service method is called with correct parameters
     */
    @Test
    @DisplayName("Should successfully add product to cart")
    void testAddToCart_Success() throws Exception {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(2)
            .build();

        CartItemResponse itemResponse = CartItemResponse.builder()
            .id(itemId)
            .productId(productId)
            .productName("Test Product")
            .quantity(2)
            .unitPrice(new BigDecimal("99.99"))
            .totalPrice(new BigDecimal("199.98"))
            .build();

        CartResponse cartResponse = CartResponse.builder()
            .cartId(cartId)
            .items(List.of(itemResponse))
            .grandTotal(new BigDecimal("199.98"))
            .totalItems(1)
            .build();

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
            .thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cartId").value(cartId.toString()))
            .andExpect(jsonPath("$.totalItems").value(1))
            .andExpect(jsonPath("$.grandTotal").value(199.98));

        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding product to cart with invalid quantity
     * 
     * Verifies that:
     * - Validation error is returned for invalid quantity
     * - HTTP 400 Bad Request status is returned
     */
    @Test
    @DisplayName("Should fail when adding product with invalid quantity")
    void testAddToCart_InvalidQuantity() throws Exception {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(0) // Invalid quantity
            .build();

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isBadRequest());

        verify(cartService, never()).addProductToCart(any(), any());
    }

    /**
     * Test successful update of cart item quantity
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Updated cart response is returned
     * - Service method is called with correct parameters
     */
    @Test
    @DisplayName("Should successfully update cart item quantity")
    void testUpdateCartItem_Success() throws Exception {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        CartItemResponse itemResponse = CartItemResponse.builder()
            .id(itemId)
            .productId(productId)
            .productName("Test Product")
            .quantity(5)
            .unitPrice(new BigDecimal("99.99"))
            .totalPrice(new BigDecimal("499.95"))
            .build();

        CartResponse cartResponse = CartResponse.builder()
            .cartId(cartId)
            .items(List.of(itemResponse))
            .grandTotal(new BigDecimal("499.95"))
            .totalItems(1)
            .build();

        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(put("/cart/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(cartId.toString()))
            .andExpect(jsonPath("$.items[0].quantity").value(5))
            .andExpect(jsonPath("$.grandTotal").value(499.95));

        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test successful removal of cart item
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Updated cart response is returned
     * - Service method is called with correct parameters
     */
    @Test
    @DisplayName("Should successfully remove cart item")
    void testRemoveCartItem_Success() throws Exception {
        // Arrange
        CartResponse cartResponse = CartResponse.builder()
            .cartId(cartId)
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.ZERO)
            .totalItems(0)
            .build();

        when(cartService.removeCartItem(userId, itemId))
            .thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(delete("/cart/items/" + itemId)
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(cartId.toString()))
            .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test removal of cart item resulting in empty cart
     * 
     * Verifies that:
     * - HTTP 204 No Content status is returned when cart becomes empty
     * - Service method is called correctly
     */
    @Test
    @DisplayName("Should return no content when removing last item from cart")
    void testRemoveCartItem_EmptyCart() throws Exception {
        // Arrange
        CartResponse cartResponse = CartResponse.builder()
            .cartId(null) // Null cartId indicates deleted cart
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.ZERO)
            .totalItems(0)
            .build();

        when(cartService.removeCartItem(userId, itemId))
            .thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(delete("/cart/items/" + itemId)
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test successful retrieval of user's cart
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Cart response contains correct data
     * - Service method is called with correct user ID
     */
    @Test
    @DisplayName("Should successfully retrieve user's cart")
    void testGetCart_Success() throws Exception {
        // Arrange
        CartItemResponse itemResponse = CartItemResponse.builder()
            .id(itemId)
            .productId(productId)
            .productName("Test Product")
            .quantity(3)
            .unitPrice(new BigDecimal("50.00"))
            .totalPrice(new BigDecimal("150.00"))
            .build();

        CartResponse cartResponse = CartResponse.builder()
            .cartId(cartId)
            .items(List.of(itemResponse))
            .grandTotal(new BigDecimal("150.00"))
            .totalItems(1)
            .build();

        when(cartService.getCart(userId))
            .thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(get("/cart")
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(cartId.toString()))
            .andExpect(jsonPath("$.totalItems").value(1))
            .andExpect(jsonPath("$.grandTotal").value(150.00))
            .andExpect(jsonPath("$.items[0].productName").value("Test Product"));

        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test retrieval of empty cart
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Empty cart response is returned correctly
     */
    @Test
    @DisplayName("Should return empty cart when no items exist")
    void testGetCart_EmptyCart() throws Exception {
        // Arrange
        CartResponse cartResponse = CartResponse.builder()
            .cartId(cartId)
            .items(new ArrayList<>())
            .grandTotal(BigDecimal.ZERO)
            .totalItems(0)
            .build();

        when(cartService.getCart(userId))
            .thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(get("/cart")
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(cartId.toString()))
            .andExpect(jsonPath("$.totalItems").value(0))
            .andExpect(jsonPath("$.grandTotal").value(0))
            .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).getCart(userId);
    }
}