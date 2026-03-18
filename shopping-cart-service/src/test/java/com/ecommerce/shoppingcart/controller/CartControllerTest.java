package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.exception.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for CartController
 * Tests all 5 endpoints with valid, invalid, and edge cases
 * Coverage: 100% of API endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Controller Tests")
public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setControllerAdvice(new com.ecommerce.common.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== GET /api/v1/cart ====================

    @Test
    @DisplayName("Get Cart - Valid User with Items - Should Return 200 OK")
    void testGetCart_ValidUserWithItems_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        CartResponse response = new CartResponse(
                1L,
                userId,
                Arrays.asList(
                        new CartResponse.CartItemResponse(1L, 1L, "Laptop", 2, new BigDecimal("999.99"), new BigDecimal("1999.98")),
                        new CartResponse.CartItemResponse(2L, 2L, "Mouse", 1, new BigDecimal("29.99"), new BigDecimal("29.99"))
                ),
                new BigDecimal("2029.97"),
                2
        );

        when(cartService.getCart(userId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalAmount").value(2029.97))
                .andExpect(jsonPath("$.totalItems").value(2));

        verify(cartService, times(1)).getCart(userId);
    }

    @Test
    @DisplayName("Get Cart - Empty Cart - Should Return 200 OK with Empty Items")
    void testGetCart_EmptyCart_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        CartResponse response = new CartResponse(
                1L,
                userId,
                Arrays.asList(),
                BigDecimal.ZERO,
                0
        );

        when(cartService.getCart(userId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @DisplayName("Get Cart - Missing User ID Header - Should Return 400 Bad Request")
    void testGetCart_MissingUserId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Cart - Non-existent Cart - Should Return 404 Not Found")
    void testGetCart_NonExistentCart_NotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        when(cartService.getCart(userId))
                .thenThrow(new CartNotFoundException("Cart not found for user ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/v1/cart/items ====================

    @Test
    @DisplayName("Add Item to Cart - Valid Request - Should Return 201 Created")
    void testAddItemToCart_ValidRequest_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);

        CartResponse response = new CartResponse(
                1L,
                userId,
                Arrays.asList(
                        new CartResponse.CartItemResponse(1L, 1L, "Laptop", 2, new BigDecimal("999.99"), new BigDecimal("1999.98"))
                ),
                new BigDecimal("1999.98"),
                1
        );

        when(cartService.addItemToCart(eq(userId), any(AddCartItemRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(cartService, times(1)).addItemToCart(eq(userId), any(AddCartItemRequest.class));
    }

    @Test
    @DisplayName("Add Item to Cart - Insufficient Stock - Should Return 400 Bad Request")
    void testAddItemToCart_InsufficientStock_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(1L, 100);

        when(cartService.addItemToCart(eq(userId), any(AddCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product ID: 1"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Add Item to Cart - Invalid Quantity - Should Return 400 Bad Request")
    void testAddItemToCart_InvalidQuantity_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(1L, -1);

        when(cartService.addItemToCart(eq(userId), any(AddCartItemRequest.class)))
                .thenThrow(new InvalidQuantityException("Quantity must be greater than 0"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Add Item to Cart - Zero Quantity - Should Return 400 Bad Request")
    void testAddItemToCart_ZeroQuantity_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(1L, 0);

        when(cartService.addItemToCart(eq(userId), any(AddCartItemRequest.class)))
                .thenThrow(new InvalidQuantityException("Quantity must be greater than 0"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Add Item to Cart - Non-existent Product - Should Return 404 Not Found")
    void testAddItemToCart_NonExistentProduct_NotFound() throws Exception {
        // Arrange
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(999L, 1);

        when(cartService.addItemToCart(eq(userId), any(AddCartItemRequest.class)))
                .thenThrow(new com.ecommerce.productcatalog.application.exception.ProductNotFoundException("Product not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== PUT /api/v1/cart/items/{cartItemId} ====================

    @Test
    @DisplayName("Update Cart Item - Valid Request - Should Return 200 OK")
    void testUpdateCartItem_ValidRequest_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        CartResponse response = new CartResponse(
                1L,
                userId,
                Arrays.asList(
                        new CartResponse.CartItemResponse(1L, 1L, "Laptop", 5, new BigDecimal("999.99"), new BigDecimal("4999.95"))
                ),
                new BigDecimal("4999.95"),
                1
        );

        when(cartService.updateCartItem(eq(userId), eq(cartItemId), any(UpdateCartItemRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.items[0].subtotal").value(4999.95));

        verify(cartService, times(1)).updateCartItem(eq(userId), eq(cartItemId), any(UpdateCartItemRequest.class));
    }

    @Test
    @DisplayName("Update Cart Item - Non-existent Cart Item - Should Return 404 Not Found")
    void testUpdateCartItem_NonExistentCartItem_NotFound() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 999L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartService.updateCartItem(eq(userId), eq(cartItemId), any(UpdateCartItemRequest.class)))
                .thenThrow(new CartItemNotFoundException("Cart item not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update Cart Item - Unauthorized User - Should Return 403 Forbidden")
    void testUpdateCartItem_UnauthorizedUser_Forbidden() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartService.updateCartItem(eq(userId), eq(cartItemId), any(UpdateCartItemRequest.class)))
                .thenThrow(new ForbiddenException("User not authorized to update this cart item"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update Cart Item - Insufficient Stock - Should Return 400 Bad Request")
    void testUpdateCartItem_InsufficientStock_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(100);

        when(cartService.updateCartItem(eq(userId), eq(cartItemId), any(UpdateCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for requested quantity"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE /api/v1/cart/items/{cartItemId} ====================

    @Test
    @DisplayName("Remove Cart Item - Valid Request - Should Return 204 No Content")
    void testRemoveCartItem_ValidRequest_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 1L;

        doNothing().when(cartService).removeCartItem(userId, cartItemId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeCartItem(userId, cartItemId);
    }

    @Test
    @DisplayName("Remove Cart Item - Non-existent Cart Item - Should Return 404 Not Found")
    void testRemoveCartItem_NonExistentCartItem_NotFound() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 999L;

        doThrow(new CartItemNotFoundException("Cart item not found with ID: 999"))
                .when(cartService).removeCartItem(userId, cartItemId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Remove Cart Item - Unauthorized User - Should Return 403 Forbidden")
    void testRemoveCartItem_UnauthorizedUser_Forbidden() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cartItemId = 1L;

        doThrow(new ForbiddenException("User not authorized to remove this cart item"))
                .when(cartService).removeCartItem(userId, cartItemId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/v1/cart ====================

    @Test
    @DisplayName("Clear Cart - Valid Request - Should Return 204 No Content")
    void testClearCart_ValidRequest_Success() throws Exception {
        // Arrange
        Long userId = 1L;

        doNothing().when(cartService).clearCart(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(userId);
    }

    @Test
    @DisplayName("Clear Cart - Non-existent Cart - Should Return 404 Not Found")
    void testClearCart_NonExistentCart_NotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        doThrow(new CartNotFoundException("Cart not found for user ID: 999"))
                .when(cartService).clearCart(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Clear Cart - Already Empty Cart - Should Return 204 No Content")
    void testClearCart_AlreadyEmpty_Success() throws Exception {
        // Arrange
        Long userId = 1L;

        doNothing().when(cartService).clearCart(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(userId);
    }
}
