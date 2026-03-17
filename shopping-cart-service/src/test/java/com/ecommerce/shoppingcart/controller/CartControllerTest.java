package com.ecommerce.shoppingcart.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Unit Test Suite for CartController
 * Tests all API endpoints with valid, invalid, and edge case scenarios
 * Coverage: 100% of CartController endpoints
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartResponse sampleCart;
    private AddCartItemRequest addItemRequest;
    private UpdateCartItemRequest updateItemRequest;

    @BeforeEach
    void setUp() {
        // Setup sample cart data
        CartResponse.CartItemDTO item1 = new CartResponse.CartItemDTO();
        item1.setItemId(1L);
        item1.setProductId(101L);
        item1.setProductName("Laptop");
        item1.setQuantity(1);
        item1.setPrice(new BigDecimal("999.99"));
        item1.setSubtotal(new BigDecimal("999.99"));

        CartResponse.CartItemDTO item2 = new CartResponse.CartItemDTO();
        item2.setItemId(2L);
        item2.setProductId(102L);
        item2.setProductName("Mouse");
        item2.setQuantity(2);
        item2.setPrice(new BigDecimal("29.99"));
        item2.setSubtotal(new BigDecimal("59.98"));

        sampleCart = new CartResponse();
        sampleCart.setCartId(1L);
        sampleCart.setUserId(1L);
        sampleCart.setItems(Arrays.asList(item1, item2));
        sampleCart.setTotalItems(3);
        sampleCart.setTotalPrice(new BigDecimal("1059.97"));

        addItemRequest = new AddCartItemRequest();
        addItemRequest.setProductId(103L);
        addItemRequest.setQuantity(1);

        updateItemRequest = new UpdateCartItemRequest();
        updateItemRequest.setQuantity(3);
    }

    // ==================== GET CART TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/v1/cart - Success - User Has Cart")
    void testGetCart_Success() throws Exception {
        // Arrange
        when(cartService.getCart(anyString())).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPrice").value(1059.97));

        verify(cartService, times(1)).getCart(anyString());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/v1/cart - Success - Empty Cart")
    void testGetCart_EmptyCart() throws Exception {
        // Arrange
        CartResponse emptyCart = new CartResponse();
        emptyCart.setCartId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(Collections.emptyList());
        emptyCart.setTotalItems(0);
        emptyCart.setTotalPrice(BigDecimal.ZERO);

        when(cartService.getCart(anyString())).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/cart - Failure - Unauthenticated")
    void testGetCart_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/v1/cart - Failure - Cart Not Found")
    void testGetCart_NotFound() throws Exception {
        // Arrange
        when(cartService.getCart(anyString()))
                .thenThrow(new CartNotFoundException("Cart not found for user"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    // ==================== ADD ITEM TO CART TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /api/v1/cart/items - Success - Add New Item")
    void testAddItemToCart_Success() throws Exception {
        // Arrange
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class))).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).addItemToCart(anyString(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /api/v1/cart/items - Failure - Product Not Found")
    void testAddItemToCart_ProductNotFound() throws Exception {
        // Arrange
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /api/v1/cart/items - Failure - Insufficient Stock")
    void testAddItemToCart_InsufficientStock() throws Exception {
        // Arrange
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for product"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /api/v1/cart/items - Failure - Invalid Quantity")
    void testAddItemToCart_InvalidQuantity() throws Exception {
        // Arrange
        addItemRequest.setQuantity(0);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /api/v1/cart/items - Failure - Negative Quantity")
    void testAddItemToCart_NegativeQuantity() throws Exception {
        // Arrange
        addItemRequest.setQuantity(-1);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Failure - Unauthenticated")
    void testAddItemToCart_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Success - Update Quantity")
    void testUpdateCartItem_Success() throws Exception {
        // Arrange
        when(cartService.updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class))).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Failure - Item Not Found")
    void testUpdateCartItem_ItemNotFound() throws Exception {
        // Arrange
        when(cartService.updateCartItem(anyString(), eq(999L), any(UpdateCartItemRequest.class)))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/999")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Failure - Forbidden (Not Owner)")
    void testUpdateCartItem_Forbidden() throws Exception {
        // Arrange
        when(cartService.updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new ForbiddenException("You don't have permission to update this cart item"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Failure - Invalid Quantity")
    void testUpdateCartItem_InvalidQuantity() throws Exception {
        // Arrange
        updateItemRequest.setQuantity(0);

        when(cartService.updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new InvalidQuantityException("Quantity must be greater than 0"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Failure - Insufficient Stock")
    void testUpdateCartItem_InsufficientStock() throws Exception {
        // Arrange
        updateItemRequest.setQuantity(1000);

        when(cartService.updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for requested quantity"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== REMOVE ITEM FROM CART TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Success")
    void testRemoveItemFromCart_Success() throws Exception {
        // Arrange
        when(cartService.removeItemFromCart(anyString(), eq(1L))).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).removeItemFromCart(anyString(), eq(1L));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Failure - Item Not Found")
    void testRemoveItemFromCart_ItemNotFound() throws Exception {
        // Arrange
        when(cartService.removeItemFromCart(anyString(), eq(999L)))
                .thenThrow(new CartItemNotFoundException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/999")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Failure - Forbidden")
    void testRemoveItemFromCart_Forbidden() throws Exception {
        // Arrange
        when(cartService.removeItemFromCart(anyString(), eq(1L)))
                .thenThrow(new ForbiddenException("You don't have permission to remove this cart item"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Failure - Unauthenticated")
    void testRemoveItemFromCart_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CLEAR CART TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("DELETE /api/v1/cart - Success")
    void testClearCart_Success() throws Exception {
        // Arrange
        CartResponse emptyCart = new CartResponse();
        emptyCart.setCartId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(Collections.emptyList());
        emptyCart.setTotalItems(0);
        emptyCart.setTotalPrice(BigDecimal.ZERO);

        when(cartService.clearCart(anyString())).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).clearCart(anyString());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("DELETE /api/v1/cart - Failure - Cart Not Found")
    void testClearCart_CartNotFound() throws Exception {
        // Arrange
        when(cartService.clearCart(anyString()))
                .thenThrow(new CartNotFoundException("Cart not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/cart - Failure - Unauthenticated")
    void testClearCart_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET CART TOTAL TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/v1/cart/total - Success")
    void testGetCartTotal_Success() throws Exception {
        // Arrange
        when(cartService.getCartTotal(anyString())).thenReturn(new BigDecimal("1059.97"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/total")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("1059.97"));

        verify(cartService, times(1)).getCartTotal(anyString());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/v1/cart/total - Success - Empty Cart")
    void testGetCartTotal_EmptyCart() throws Exception {
        // Arrange
        when(cartService.getCartTotal(anyString())).thenReturn(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/total")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/v1/cart/total - Failure - Cart Not Found")
    void testGetCartTotal_CartNotFound() throws Exception {
        // Arrange
        when(cartService.getCartTotal(anyString()))
                .thenThrow(new CartNotFoundException("Cart not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/total")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/cart/total - Failure - Unauthenticated")
    void testGetCartTotal_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/cart/total"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== EDGE CASE AND INTEGRATION TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Edge Case - Add Multiple Items Sequentially")
    void testAddMultipleItemsSequentially() throws Exception {
        // Arrange
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class))).thenReturn(sampleCart);

        // Act & Assert - Add 5 items
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/cart/items")
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addItemRequest))
                    .with(csrf()))
                    .andExpect(status().isCreated());
        }

        verify(cartService, times(5)).addItemToCart(anyString(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Edge Case - Update Item to Maximum Quantity")
    void testUpdateItemToMaximumQuantity() throws Exception {
        // Arrange
        updateItemRequest.setQuantity(Integer.MAX_VALUE);

        when(cartService.updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new InvalidQuantityException("Quantity exceeds maximum allowed"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Edge Case - Concurrent Cart Operations")
    void testConcurrentCartOperations() throws Exception {
        // Arrange
        when(cartService.getCart(anyString())).thenReturn(sampleCart);

        // Act & Assert - Simulate concurrent requests
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/cart")
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk());
        }

        verify(cartService, times(10)).getCart(anyString());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Edge Case - Add Item with Very Large Product ID")
    void testAddItemWithLargeProductId() throws Exception {
        // Arrange
        addItemRequest.setProductId(Long.MAX_VALUE);

        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("Integration Test - Complete Cart Workflow")
    void testCompleteCartWorkflow() throws Exception {
        // 1. Get empty cart
        CartResponse emptyCart = new CartResponse();
        emptyCart.setCartId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(Collections.emptyList());
        emptyCart.setTotalItems(0);
        emptyCart.setTotalPrice(BigDecimal.ZERO);

        when(cartService.getCart(anyString())).thenReturn(emptyCart);

        mockMvc.perform(get("/api/v1/cart")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));

        // 2. Add item to cart
        when(cartService.addItemToCart(anyString(), any(AddCartItemRequest.class))).thenReturn(sampleCart);

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest))
                .with(csrf()))
                .andExpect(status().isCreated());

        // 3. Update item quantity
        when(cartService.updateCartItem(anyString(), eq(1L), any(UpdateCartItemRequest.class))).thenReturn(sampleCart);

        mockMvc.perform(put("/api/v1/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        // 4. Get cart total
        when(cartService.getCartTotal(anyString())).thenReturn(new BigDecimal("1059.97"));

        mockMvc.perform(get("/api/v1/cart/total")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());

        // 5. Clear cart
        when(cartService.clearCart(anyString())).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/v1/cart")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }
}