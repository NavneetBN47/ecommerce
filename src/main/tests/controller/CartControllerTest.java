package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.service.CartService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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
    private UUID cartId;
    private UUID itemId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        cartId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Setup cart item responses
        CartItemResponse item1 = new CartItemResponse(
            itemId,
            productId,
            "Dell XPS 15 Laptop",
            new BigDecimal("1299.99"),
            2,
            new BigDecimal("2599.98")
        );

        CartItemResponse item2 = new CartItemResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Wireless Mouse",
            new BigDecimal("29.99"),
            1,
            new BigDecimal("29.99")
        );

        // Setup cart response
        cartResponse = new CartResponse(
            cartId,
            UUID.randomUUID(),
            Arrays.asList(item1, item2),
            new BigDecimal("2629.97"),
            3,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Setup add item request
        addItemRequest = new AddCartItemRequest(
            productId,
            2
        );

        // Setup update item request
        updateItemRequest = new UpdateCartItemRequest(3);
    }

    // ==================== GET CART TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("GET /api/v1/cart - Authenticated User - Should Return 200")
    void testGetCart_AuthenticatedUser_ShouldReturn200() throws Exception {
        when(cartService.getCart(eq("john.doe@example.com")))
            .thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(cartId.toString()))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.totalPrice").value(2629.97))
            .andExpect(jsonPath("$.totalItems").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/cart - Unauthenticated User - Should Return 401")
    void testGetCart_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("GET /api/v1/cart - Empty Cart - Should Return 200")
    void testGetCart_EmptyCart_ShouldReturn200() throws Exception {
        CartResponse emptyCart = new CartResponse(
            cartId,
            UUID.randomUUID(),
            Collections.emptyList(),
            BigDecimal.ZERO,
            0,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(cartService.getCart(eq("john.doe@example.com")))
            .thenReturn(emptyCart);

        mockMvc.perform(get("/api/v1/cart")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.totalPrice").value(0))
            .andExpect(jsonPath("$.totalItems").value(0));
    }

    // ==================== ADD ITEM TO CART TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("POST /api/v1/cart/items - Valid Request - Should Return 201")
    void testAddItemToCart_ValidRequest_ShouldReturn201() throws Exception {
        when(cartService.addItemToCart(eq("john.doe@example.com"), any(AddCartItemRequest.class)))
            .thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.totalPrice").exists());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("POST /api/v1/cart/items - Quantity Below Minimum - Should Return 400")
    void testAddItemToCart_QuantityBelowMinimum_ShouldReturn400() throws Exception {
        AddCartItemRequest invalidRequest = new AddCartItemRequest(productId, 0);

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("POST /api/v1/cart/items - Quantity Exceeds Maximum - Should Return 400")
    void testAddItemToCart_QuantityExceedsMaximum_ShouldReturn400() throws Exception {
        AddCartItemRequest invalidRequest = new AddCartItemRequest(productId, 100);

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("POST /api/v1/cart/items - Missing Product ID - Should Return 400")
    void testAddItemToCart_MissingProductId_ShouldReturn400() throws Exception {
        AddCartItemRequest invalidRequest = new AddCartItemRequest(null, 2);

        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Unauthenticated User - Should Return 401")
    void testAddItemToCart_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE CART ITEM TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Valid Request - Should Return 200")
    void testUpdateCartItem_ValidRequest_ShouldReturn200() throws Exception {
        when(cartService.updateCartItem(eq("john.doe@example.com"), eq(itemId), any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Quantity Below Minimum - Should Return 400")
    void testUpdateCartItem_QuantityBelowMinimum_ShouldReturn400() throws Exception {
        UpdateCartItemRequest invalidRequest = new UpdateCartItemRequest(0);

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Quantity Exceeds Maximum - Should Return 400")
    void testUpdateCartItem_QuantityExceedsMaximum_ShouldReturn400() throws Exception {
        UpdateCartItemRequest invalidRequest = new UpdateCartItemRequest(100);

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Invalid Item ID Format - Should Return 400")
    void testUpdateCartItem_InvalidItemIdFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", "invalid-uuid")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{itemId} - Unauthenticated User - Should Return 401")
    void testUpdateCartItem_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ==================== REMOVE ITEM FROM CART TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Valid Request - Should Return 200")
    void testRemoveItemFromCart_ValidRequest_ShouldReturn200() throws Exception {
        when(cartService.removeItemFromCart(eq("john.doe@example.com"), eq(itemId)))
            .thenReturn(cartResponse);

        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Invalid Item ID Format - Should Return 400")
    void testRemoveItemFromCart_InvalidItemIdFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", "invalid-uuid")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Item Not Found - Should Return 404")
    void testRemoveItemFromCart_ItemNotFound_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(cartService.removeItemFromCart(eq("john.doe@example.com"), eq(nonExistentId)))
            .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", nonExistentId)
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{itemId} - Unauthenticated User - Should Return 401")
    void testRemoveItemFromCart_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    // ==================== CLEAR CART TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("DELETE /api/v1/cart - Valid Request - Should Return 200")
    void testClearCart_ValidRequest_ShouldReturn200() throws Exception {
        CartResponse emptyCart = new CartResponse(
            cartId,
            UUID.randomUUID(),
            Collections.emptyList(),
            BigDecimal.ZERO,
            0,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(cartService.clearCart(eq("john.doe@example.com")))
            .thenReturn(emptyCart);

        mockMvc.perform(delete("/api/v1/cart")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.totalPrice").value(0))
            .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart - Unauthenticated User - Should Return 401")
    void testClearCart_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("DELETE /api/v1/cart - Already Empty Cart - Should Return 200")
    void testClearCart_AlreadyEmptyCart_ShouldReturn200() throws Exception {
        CartResponse emptyCart = new CartResponse(
            cartId,
            UUID.randomUUID(),
            Collections.emptyList(),
            BigDecimal.ZERO,
            0,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(cartService.clearCart(eq("john.doe@example.com")))
            .thenReturn(emptyCart);

        mockMvc.perform(delete("/api/v1/cart")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(0));
    }
}