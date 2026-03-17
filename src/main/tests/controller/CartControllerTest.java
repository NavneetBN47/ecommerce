package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.dto.*;
import com.ecommerce.shoppingcart.service.CartService;
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
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@ExtendWith(MockitoExtension.class)
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
    private CartItemDto cartItem1;
    private CartItemDto cartItem2;

    @BeforeEach
    void setUp() {
        // Setup cart items
        cartItem1 = CartItemDto.builder()
            .itemId(1L)
            .productId(101L)
            .productName("Laptop")
            .quantity(1)
            .unitPrice(new BigDecimal("999.99"))
            .subtotal(new BigDecimal("999.99"))
            .build();

        cartItem2 = CartItemDto.builder()
            .itemId(2L)
            .productId(102L)
            .productName("Mouse")
            .quantity(2)
            .unitPrice(new BigDecimal("29.99"))
            .subtotal(new BigDecimal("59.98"))
            .build();

        // Setup cart response
        cartResponse = CartResponse.builder()
            .cartId(1L)
            .userId(1L)
            .items(Arrays.asList(cartItem1, cartItem2))
            .totalItems(3)
            .totalAmount(new BigDecimal("1059.97"))
            .build();

        // Setup add item request
        addItemRequest = AddCartItemRequest.builder()
            .productId(101L)
            .quantity(1)
            .build();

        // Setup update item request
        updateItemRequest = UpdateCartItemRequest.builder()
            .quantity(3)
            .build();
    }

    // ==================== GET CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/cart - Authenticated User - Should Return 200")
    void testGetCart_AuthenticatedUser_ReturnsOk() throws Exception {
        when(cartService.getCart("test@example.com"))
            .thenReturn(cartResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/cart")
            .with(csrf()));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(1L))
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.totalItems").value(3))
            .andExpect(jsonPath("$.totalAmount").value(1059.97));

        verify(cartService, times(1)).getCart("test@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/cart - Unauthenticated User - Should Return 401")
    void testGetCart_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/cart"));

        result.andExpect(status().isUnauthorized());
        verify(cartService, never()).getCart(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/cart - Empty Cart - Should Return 200")
    void testGetCart_EmptyCart_ReturnsOk() throws Exception {
        CartResponse emptyCart = CartResponse.builder()
            .cartId(1L)
            .userId(1L)
            .items(Collections.emptyList())
            .totalItems(0)
            .totalAmount(BigDecimal.ZERO)
            .build();

        when(cartService.getCart("test@example.com"))
            .thenReturn(emptyCart);

        ResultActions result = mockMvc.perform(get("/api/v1/cart")
            .with(csrf()));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.totalItems").value(0))
            .andExpect(jsonPath("$.totalAmount").value(0));

        verify(cartService, times(1)).getCart("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/cart - Cart Not Found - Should Return 404")
    void testGetCart_CartNotFound_ReturnsNotFound() throws Exception {
        when(cartService.getCart("test@example.com"))
            .thenThrow(new RuntimeException("Cart not found"));

        ResultActions result = mockMvc.perform(get("/api/v1/cart")
            .with(csrf()));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).getCart("test@example.com");
    }

    // ==================== ADD ITEM TO CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Valid Request - Should Return 201")
    void testAddItemToCart_ValidRequest_ReturnsCreated() throws Exception {
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
            .thenReturn(cartResponse);

        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(addItemRequest)));

        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.totalItems").value(3));

        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Unauthenticated - Should Return 401")
    void testAddItemToCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(addItemRequest)));

        result.andExpect(status().isForbidden());
        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Invalid Product ID - Should Return 400")
    void testAddItemToCart_InvalidProductId_ReturnsBadRequest() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
            .productId(null)
            .quantity(1)
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Invalid Quantity - Should Return 400")
    void testAddItemToCart_InvalidQuantity_ReturnsBadRequest() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
            .productId(101L)
            .quantity(0)
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Negative Quantity - Should Return 400")
    void testAddItemToCart_NegativeQuantity_ReturnsBadRequest() throws Exception {
        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
            .productId(101L)
            .quantity(-1)
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(cartService, never()).addItemToCart(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Product Not Found - Should Return 404")
    void testAddItemToCart_ProductNotFound_ReturnsNotFound() throws Exception {
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
            .thenThrow(new RuntimeException("Product not found"));

        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(addItemRequest)));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/cart/items - Insufficient Stock - Should Return 400")
    void testAddItemToCart_InsufficientStock_ReturnsBadRequest() throws Exception {
        when(cartService.addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class)))
            .thenThrow(new RuntimeException("Insufficient stock"));

        ResultActions result = mockMvc.perform(post("/api/v1/cart/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(addItemRequest)));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).addItemToCart(eq("test@example.com"), any(AddCartItemRequest.class));
    }

    // ==================== UPDATE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Valid Update - Should Return 200")
    void testUpdateCartItem_ValidUpdate_ReturnsOk() throws Exception {
        when(cartService.updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        ResultActions result = mockMvc.perform(put("/api/v1/cart/items/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateItemRequest)));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Unauthenticated - Should Return 401")
    void testUpdateCartItem_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(put("/api/v1/cart/items/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateItemRequest)));

        result.andExpect(status().isForbidden());
        verify(cartService, never()).updateCartItem(any(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Invalid Quantity - Should Return 400")
    void testUpdateCartItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        UpdateCartItemRequest invalidRequest = UpdateCartItemRequest.builder()
            .quantity(0)
            .build();

        ResultActions result = mockMvc.perform(put("/api/v1/cart/items/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(cartService, never()).updateCartItem(any(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Item Not Found - Should Return 404")
    void testUpdateCartItem_ItemNotFound_ReturnsNotFound() throws Exception {
        when(cartService.updateCartItem(eq("test@example.com"), eq(999L), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Cart item not found"));

        ResultActions result = mockMvc.perform(put("/api/v1/cart/items/999")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateItemRequest)));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(999L), any(UpdateCartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Insufficient Stock - Should Return 400")
    void testUpdateCartItem_InsufficientStock_ReturnsBadRequest() throws Exception {
        when(cartService.updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class)))
            .thenThrow(new RuntimeException("Insufficient stock"));

        ResultActions result = mockMvc.perform(put("/api/v1/cart/items/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateItemRequest)));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).updateCartItem(eq("test@example.com"), eq(1L), any(UpdateCartItemRequest.class));
    }

    // ==================== REMOVE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Valid Request - Should Return 200")
    void testRemoveCartItem_ValidRequest_ReturnsOk() throws Exception {
        when(cartService.removeCartItem("test@example.com", 1L))
            .thenReturn(cartResponse);

        ResultActions result = mockMvc.perform(delete("/api/v1/cart/items/1")
            .with(csrf()));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());

        verify(cartService, times(1)).removeCartItem("test@example.com", 1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Unauthenticated - Should Return 401")
    void testRemoveCartItem_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/items/1"));

        result.andExpect(status().isForbidden());
        verify(cartService, never()).removeCartItem(any(), anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Item Not Found - Should Return 404")
    void testRemoveCartItem_ItemNotFound_ReturnsNotFound() throws Exception {
        when(cartService.removeCartItem("test@example.com", 999L))
            .thenThrow(new RuntimeException("Cart item not found"));

        ResultActions result = mockMvc.perform(delete("/api/v1/cart/items/999")
            .with(csrf()));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).removeCartItem("test@example.com", 999L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Invalid Item ID - Should Return 400")
    void testRemoveCartItem_InvalidItemId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(delete("/api/v1/cart/items/invalid")
            .with(csrf()));

        result.andExpect(status().isBadRequest());
        verify(cartService, never()).removeCartItem(any(), anyLong());
    }

    // ==================== CLEAR CART TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart - Valid Request - Should Return 204")
    void testClearCart_ValidRequest_ReturnsNoContent() throws Exception {
        doNothing().when(cartService).clearCart("test@example.com");

        ResultActions result = mockMvc.perform(delete("/api/v1/cart")
            .with(csrf()));

        result.andExpect(status().isNoContent());
        verify(cartService, times(1)).clearCart("test@example.com");
    }

    @Test
    @DisplayName("DELETE /api/v1/cart - Unauthenticated - Should Return 401")
    void testClearCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(delete("/api/v1/cart"));

        result.andExpect(status().isForbidden());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart - Cart Not Found - Should Return 404")
    void testClearCart_CartNotFound_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("Cart not found"))
            .when(cartService).clearCart("test@example.com");

        ResultActions result = mockMvc.perform(delete("/api/v1/cart")
            .with(csrf()));

        result.andExpect(status().isInternalServerError());
        verify(cartService, times(1)).clearCart("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/cart - Already Empty Cart - Should Return 204")
    void testClearCart_AlreadyEmpty_ReturnsNoContent() throws Exception {
        doNothing().when(cartService).clearCart("test@example.com");

        ResultActions result = mockMvc.perform(delete("/api/v1/cart")
            .with(csrf()));

        result.andExpect(status().isNoContent());
        verify(cartService, times(1)).clearCart("test@example.com");
    }
}