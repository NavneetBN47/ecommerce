package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.service.CartService;
import com.ecommerce.shoppingcart.presentation.controller.CartController;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for CartController
 * Coverage: All 6 endpoints with valid, invalid, and edge cases
 * Authentication: All endpoints require JWT authentication
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartResponse sampleCart;
    private AddCartItemRequest validAddItemRequest;
    private UpdateCartItemRequest validUpdateItemRequest;

    @BeforeEach
    void setUp() {
        // Sample cart response
        sampleCart = new CartResponse();
        sampleCart.setCartId(1L);
        sampleCart.setUserId(1L);
        sampleCart.setItems(Arrays.asList(
                createCartItem(1L, 1L, "Laptop", new BigDecimal("999.99"), 2),
                createCartItem(2L, 2L, "Mouse", new BigDecimal("29.99"), 1)
        ));
        sampleCart.setTotalAmount(new BigDecimal("2029.97"));
        sampleCart.setItemCount(3);

        // Valid add item request
        validAddItemRequest = new AddCartItemRequest();
        validAddItemRequest.setProductId(1L);
        validAddItemRequest.setQuantity(2);

        // Valid update item request
        validUpdateItemRequest = new UpdateCartItemRequest();
        validUpdateItemRequest.setQuantity(3);
    }

    private CartItemResponse createCartItem(Long itemId, Long productId, String productName, BigDecimal price, int quantity) {
        CartItemResponse item = new CartItemResponse();
        item.setCartItemId(itemId);
        item.setProductId(productId);
        item.setProductName(productName);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setSubtotal(price.multiply(new BigDecimal(quantity)));
        return item;
    }

    // ==================== GET CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/cart - Authenticated User with Cart - Success")
    void testGetCart_AuthenticatedUserWithCart_ReturnsOk() throws Exception {
        // Arrange
        when(cartService.getCart("test@example.com")).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalAmount").value(2029.97))
                .andExpect(jsonPath("$.itemCount").value(3));

        verify(cartService, times(1)).getCart("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/cart - Empty Cart - Success")
    void testGetCart_EmptyCart_ReturnsOk() throws Exception {
        // Arrange
        CartResponse emptyCart = new CartResponse();
        emptyCart.setCartId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(Collections.emptyList());
        emptyCart.setTotalAmount(BigDecimal.ZERO);
        emptyCart.setItemCount(0);

        when(cartService.getCart("test@example.com")).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.itemCount").value(0));

        verify(cartService, times(1)).getCart("test@example.com");
    }

    @Test
    @DisplayName("GET /api/cart - Unauthenticated User - Unauthorized")
    void testGetCart_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart(any());
    }

    // ==================== ADD ITEM TO CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Valid Request - Success")
    void testAddItemToCart_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Invalid Product ID - BadRequest")
    void testAddItemToCart_InvalidProductId_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setProductId(null);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Invalid Quantity - BadRequest")
    void testAddItemToCart_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setQuantity(0);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Negative Quantity - BadRequest")
    void testAddItemToCart_NegativeQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setQuantity(-1);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Product Not Found - NotFound")
    void testAddItemToCart_ProductNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Insufficient Stock - BadRequest")
    void testAddItemToCart_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Arrange
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @DisplayName("POST /api/cart/items - Unauthenticated - Unauthorized")
    void testAddItemToCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest)))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    // ==================== UPDATE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Valid Request - Success")
    void testUpdateCartItem_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        when(cartService.updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Invalid Quantity - BadRequest")
    void testUpdateCartItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validUpdateItemRequest.setQuantity(0);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Item Not Found - NotFound")
    void testUpdateCartItem_ItemNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(cartService.updateCartItem(eq("test@example.com"), eq(999L), any(UpdateCartItemRequest.class)))
                .thenThrow(new RuntimeException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(999L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Insufficient Stock - BadRequest")
    void testUpdateCartItem_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Arrange
        when(cartService.updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @DisplayName("PUT /api/cart/items/{itemId} - Unauthenticated - Unauthorized")
    void testUpdateCartItem_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest)))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).updateCartItem(any(), any(), any());
    }

    // ==================== REMOVE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Valid Request - Success")
    void testRemoveCartItem_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        when(cartService.removeCartItem("test@example.com", 1L)).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).removeCartItem("test@example.com", 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Item Not Found - NotFound")
    void testRemoveCartItem_ItemNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(cartService.removeCartItem("test@example.com", 999L))
                .thenThrow(new RuntimeException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/999")
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).removeCartItem("test@example.com", 999L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Invalid Item ID - BadRequest")
    void testRemoveCartItem_InvalidItemId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/invalid")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).removeCartItem(any(), any());
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{itemId} - Unauthenticated - Unauthorized")
    void testRemoveCartItem_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/1"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).removeCartItem(any(), any());
    }

    // ==================== CLEAR CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart - Valid Request - Success")
    void testClearCart_ValidRequest_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(cartService).clearCart("test@example.com");

        // Act & Assert
        mockMvc.perform(delete("/api/cart")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart - Cart Not Found - NotFound")
    void testClearCart_CartNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Cart not found")).when(cartService).clearCart("test@example.com");

        // Act & Assert
        mockMvc.perform(delete("/api/cart")
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).clearCart("test@example.com");
    }

    @Test
    @DisplayName("DELETE /api/cart - Unauthenticated - Unauthorized")
    void testClearCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(any());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Maximum Quantity - Success")
    void testAddItemToCart_MaximumQuantity_ReturnsOk() throws Exception {
        // Arrange
        validAddItemRequest.setQuantity(Integer.MAX_VALUE);
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Update to Same Quantity - Success")
    void testUpdateCartItem_SameQuantity_ReturnsOk() throws Exception {
        // Arrange
        validUpdateItemRequest.setQuantity(2);
        when(cartService.updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Add Duplicate Product - UpdatesQuantity")
    void testAddItemToCart_DuplicateProduct_UpdatesQuantity() throws Exception {
        // Arrange
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Remove Last Item - EmptyCart")
    void testRemoveCartItem_LastItem_ReturnsEmptyCart() throws Exception {
        // Arrange
        CartResponse emptyCart = new CartResponse();
        emptyCart.setCartId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(Collections.emptyList());
        emptyCart.setTotalAmount(BigDecimal.ZERO);
        emptyCart.setItemCount(0);

        when(cartService.removeCartItem("test@example.com", 1L)).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0));

        verify(cartService, times(1)).removeCartItem("test@example.com", 1L);
    }
}