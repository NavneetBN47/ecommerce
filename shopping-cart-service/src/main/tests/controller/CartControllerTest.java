package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.service.CartService;
import com.ecommerce.shoppingcart.infrastructure.security.JwtTokenService;
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
 * Comprehensive Unit Tests for CartController
 * Tests all API endpoints with valid, invalid, and edge case scenarios
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(CartController.class)
@DisplayName("Cart Controller Unit Tests")
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartResponse sampleCart;
    private AddCartItemRequest validAddItemRequest;
    private UpdateCartItemRequest validUpdateItemRequest;

    @BeforeEach
    void setUp() {
        // Setup sample cart data
        CartItemResponse item1 = new CartItemResponse();
        item1.setCartItemId(1L);
        item1.setProductId(101L);
        item1.setProductName("Laptop");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("999.99"));
        item1.setSubtotal(new BigDecimal("1999.98"));

        CartItemResponse item2 = new CartItemResponse();
        item2.setCartItemId(2L);
        item2.setProductId(102L);
        item2.setProductName("Mouse");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("29.99"));
        item2.setSubtotal(new BigDecimal("29.99"));

        sampleCart = new CartResponse();
        sampleCart.setCartId(1L);
        sampleCart.setUserId(1L);
        sampleCart.setItems(Arrays.asList(item1, item2));
        sampleCart.setTotalItems(3);
        sampleCart.setTotalPrice(new BigDecimal("2029.97"));

        validAddItemRequest = new AddCartItemRequest();
        validAddItemRequest.setProductId(103L);
        validAddItemRequest.setQuantity(1);

        validUpdateItemRequest = new UpdateCartItemRequest();
        validUpdateItemRequest.setQuantity(3);
    }

    // ==================== GET CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/cart - Authenticated User - Should Return 200")
    void testGetCart_AuthenticatedUser_ReturnsCart() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.getCart(eq(1L))).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPrice").value(2029.97));

        verify(cartService, times(1)).getCart(eq(1L));
    }

    @Test
    @DisplayName("GET /api/cart - No Authentication - Should Return 401")
    void testGetCart_NoAuth_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart(anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/cart - Empty Cart - Should Return 200 with Empty Items")
    void testGetCart_EmptyCart_ReturnsEmptyCart() throws Exception {
        // Arrange
        CartResponse emptyCart = new CartResponse();
        emptyCart.setCartId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(Collections.emptyList());
        emptyCart.setTotalItems(0);
        emptyCart.setTotalPrice(BigDecimal.ZERO);

        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.getCart(eq(1L))).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        verify(cartService, times(1)).getCart(eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/cart - Invalid Token - Should Return 401")
    void testGetCart_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString()))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCart(anyLong());
    }

    // ==================== ADD ITEM TO CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Valid Request - Should Return 201")
    void testAddItemToCart_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Invalid Product ID - Should Return 400")
    void testAddItemToCart_InvalidProductId_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setProductId(null);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Invalid Quantity (Zero) - Should Return 400")
    void testAddItemToCart_ZeroQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setQuantity(0);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Negative Quantity - Should Return 400")
    void testAddItemToCart_NegativeQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setQuantity(-1);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Product Not Found - Should Return 404")
    void testAddItemToCart_ProductNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Insufficient Stock - Should Return 400")
    void testAddItemToCart_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.addItemToCart(eq(1L), any(AddCartItemRequest.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addItemToCart(eq(1L), any(AddCartItemRequest.class));
    }

    @Test
    @DisplayName("POST /api/cart/items - No Authentication - Should Return 401")
    void testAddItemToCart_NoAuth_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    // ==================== UPDATE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Valid Update - Should Return 200")
    void testUpdateCartItem_ValidRequest_ReturnsUpdated() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class)))
                .thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Invalid Quantity - Should Return 400")
    void testUpdateCartItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validUpdateItemRequest.setQuantity(0);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(anyLong(), anyLong(), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Item Not Found - Should Return 404")
    void testUpdateCartItem_ItemNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.updateCartItem(eq(1L), eq(999L), any(UpdateCartItemRequest.class)))
                .thenThrow(new RuntimeException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/999")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(999L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Unauthorized Access - Should Return 403")
    void testUpdateCartItem_UnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new RuntimeException("Forbidden"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class));
    }

    // ==================== REMOVE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Valid Request - Should Return 200")
    void testRemoveCartItem_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.removeCartItem(eq(1L), eq(1L))).thenReturn(sampleCart);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1));

        verify(cartService, times(1)).removeCartItem(eq(1L), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Item Not Found - Should Return 404")
    void testRemoveCartItem_ItemNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.removeCartItem(eq(1L), eq(999L)))
                .thenThrow(new RuntimeException("Cart item not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/999")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeCartItem(eq(1L), eq(999L));
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{itemId} - No Authentication - Should Return 401")
    void testRemoveCartItem_NoAuth_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).removeCartItem(anyLong(), anyLong());
    }

    // ==================== CLEAR CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart - Valid Request - Should Return 204")
    void testClearCart_ValidRequest_ReturnsNoContent() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        doNothing().when(cartService).clearCart(eq(1L));

        // Act & Assert
        mockMvc.perform(delete("/api/cart")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(eq(1L));
    }

    @Test
    @DisplayName("DELETE /api/cart - No Authentication - Should Return 401")
    void testClearCart_NoAuth_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(anyLong());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Extremely Large Quantity - Should Return 400")
    void testAddItemToCart_ExtremelyLargeQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        validAddItemRequest.setQuantity(Integer.MAX_VALUE);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAddItemRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Null Request Body - Should Return 400")
    void testAddItemToCart_NullBody_ReturnsBadRequest() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/cart/items - Malformed JSON - Should Return 400")
    void testAddItemToCart_MalformedJSON_ReturnsBadRequest() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyLong(), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/cart/items/{itemId} - Invalid Item ID Format - Should Return 400")
    void testRemoveCartItem_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/invalid")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).removeCartItem(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/cart/items/{itemId} - Concurrent Modification - Should Handle Gracefully")
    void testUpdateCartItem_ConcurrentModification_HandlesGracefully() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);
        when(cartService.updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class)))
                .thenThrow(new RuntimeException("Concurrent modification detected"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/1")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateItemRequest))
                .with(csrf()))
                .andExpect(status().isConflict());

        verify(cartService, times(1)).updateCartItem(eq(1L), eq(1L), any(UpdateCartItemRequest.class));
    }
}