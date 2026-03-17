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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CartController.class)
@DisplayName("Cart Controller Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartResponseDTO cartResponseDTO;
    private String userId;
    private String cartId;
    private String itemId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        cartId = UUID.randomUUID().toString();
        itemId = UUID.randomUUID().toString();

        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setItemId(itemId);
        cartItemDTO.setProductId(UUID.randomUUID().toString());
        cartItemDTO.setProductName("Test Product");
        cartItemDTO.setQuantity(2);
        cartItemDTO.setPrice(new BigDecimal("99.99"));
        cartItemDTO.setSubtotal(new BigDecimal("199.98"));

        cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setCartId(cartId);
        cartResponseDTO.setUserId(userId);
        cartResponseDTO.setItems(Arrays.asList(cartItemDTO));
        cartResponseDTO.setTotalItems(2);
        cartResponseDTO.setTotalPrice(new BigDecimal("199.98"));
        cartResponseDTO.setCreatedAt(LocalDateTime.now());
        cartResponseDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/carts/{userId} - Valid User ID - Success")
    void testGetCart_ValidUserId_ReturnsOk() throws Exception {
        when(cartService.getCartByUserId(userId)).thenReturn(cartResponseDTO);

        mockMvc.perform(get("/api/v1/carts/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(199.98));

        verify(cartService, times(1)).getCartByUserId(userId);
    }

    @Test
    @DisplayName("GET /api/v1/carts/{userId} - No Authentication - Unauthorized")
    void testGetCart_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/carts/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCartByUserId(anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/carts/{userId} - Cart Not Found - Not Found")
    void testGetCart_NotFound_ReturnsNotFound() throws Exception {
        when(cartService.getCartByUserId(userId))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/api/v1/carts/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).getCartByUserId(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/items - Valid Item - Created")
    void testAddItemToCart_ValidItem_ReturnsCreated() throws Exception {
        AddItemToCartDTO addItemDTO = new AddItemToCartDTO();
        addItemDTO.setProductId(UUID.randomUUID().toString());
        addItemDTO.setQuantity(2);

        when(cartService.addItemToCart(eq(userId), any(AddItemToCartDTO.class)))
                .thenReturn(cartResponseDTO);

        mockMvc.perform(post("/api/v1/carts/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemDTO))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.totalItems").value(2));

        verify(cartService, times(1)).addItemToCart(eq(userId), any(AddItemToCartDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/items - Invalid Quantity - Bad Request")
    void testAddItemToCart_InvalidQuantity_ReturnsBadRequest() throws Exception {
        AddItemToCartDTO addItemDTO = new AddItemToCartDTO();
        addItemDTO.setProductId(UUID.randomUUID().toString());
        addItemDTO.setQuantity(0);

        mockMvc.perform(post("/api/v1/carts/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(anyString(), any(AddItemToCartDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/items - Product Not Found - Not Found")
    void testAddItemToCart_ProductNotFound_ReturnsNotFound() throws Exception {
        AddItemToCartDTO addItemDTO = new AddItemToCartDTO();
        addItemDTO.setProductId(UUID.randomUUID().toString());
        addItemDTO.setQuantity(2);

        when(cartService.addItemToCart(eq(userId), any(AddItemToCartDTO.class)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/api/v1/carts/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemDTO))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).addItemToCart(eq(userId), any(AddItemToCartDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/items - Insufficient Stock - Bad Request")
    void testAddItemToCart_InsufficientStock_ReturnsBadRequest() throws Exception {
        AddItemToCartDTO addItemDTO = new AddItemToCartDTO();
        addItemDTO.setProductId(UUID.randomUUID().toString());
        addItemDTO.setQuantity(1000);

        when(cartService.addItemToCart(eq(userId), any(AddItemToCartDTO.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        mockMvc.perform(post("/api/v1/carts/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemDTO))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).addItemToCart(eq(userId), any(AddItemToCartDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/v1/carts/{userId}/items/{itemId} - Valid Update - Success")
    void testUpdateCartItem_ValidUpdate_ReturnsOk() throws Exception {
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(5);

        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemDTO.class)))
                .thenReturn(cartResponseDTO);

        mockMvc.perform(put("/api/v1/carts/{userId}/items/{itemId}", userId, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId));

        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/v1/carts/{userId}/items/{itemId} - Invalid Quantity - Bad Request")
    void testUpdateCartItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(-1);

        mockMvc.perform(put("/api/v1/carts/{userId}/items/{itemId}", userId, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(anyString(), anyString(), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/carts/{userId}/items/{itemId} - Valid Item - Success")
    void testRemoveItemFromCart_ValidItem_ReturnsOk() throws Exception {
        when(cartService.removeItemFromCart(userId, itemId)).thenReturn(cartResponseDTO);

        mockMvc.perform(delete("/api/v1/carts/{userId}/items/{itemId}", userId, itemId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId));

        verify(cartService, times(1)).removeItemFromCart(userId, itemId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/carts/{userId}/items/{itemId} - Item Not Found - Not Found")
    void testRemoveItemFromCart_ItemNotFound_ReturnsNotFound() throws Exception {
        when(cartService.removeItemFromCart(userId, itemId))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(delete("/api/v1/carts/{userId}/items/{itemId}", userId, itemId)
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).removeItemFromCart(userId, itemId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/carts/{userId} - Clear Cart - No Content")
    void testClearCart_ValidCart_ReturnsNoContent() throws Exception {
        doNothing().when(cartService).clearCart(userId);

        mockMvc.perform(delete("/api/v1/carts/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(userId);
    }

    @Test
    @DisplayName("DELETE /api/v1/carts/{userId} - No Authentication - Unauthorized")
    void testClearCart_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/carts/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clearCart(anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/checkout - Valid Checkout - Success")
    void testCheckout_ValidCart_ReturnsOk() throws Exception {
        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setShippingAddress("123 Main St, City, State 12345");
        checkoutRequest.setPaymentMethod("CREDIT_CARD");

        CheckoutResponseDTO checkoutResponse = new CheckoutResponseDTO();
        checkoutResponse.setOrderId(UUID.randomUUID().toString());
        checkoutResponse.setTotalAmount(new BigDecimal("199.98"));
        checkoutResponse.setStatus("PENDING");
        checkoutResponse.setMessage("Checkout initiated successfully");

        when(cartService.checkout(eq(userId), any(CheckoutRequestDTO.class)))
                .thenReturn(checkoutResponse);

        mockMvc.perform(post("/api/v1/carts/{userId}/checkout", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(cartService, times(1)).checkout(eq(userId), any(CheckoutRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/checkout - Empty Cart - Bad Request")
    void testCheckout_EmptyCart_ReturnsBadRequest() throws Exception {
        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setShippingAddress("123 Main St, City, State 12345");
        checkoutRequest.setPaymentMethod("CREDIT_CARD");

        when(cartService.checkout(eq(userId), any(CheckoutRequestDTO.class)))
                .thenThrow(new RuntimeException("Cart is empty"));

        mockMvc.perform(post("/api/v1/carts/{userId}/checkout", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).checkout(eq(userId), any(CheckoutRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/carts/{userId}/checkout - Missing Shipping Address - Bad Request")
    void testCheckout_MissingShippingAddress_ReturnsBadRequest() throws Exception {
        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setPaymentMethod("CREDIT_CARD");

        mockMvc.perform(post("/api/v1/carts/{userId}/checkout", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).checkout(anyString(), any(CheckoutRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/carts/{userId}/total - Valid Cart - Success")
    void testGetCartTotal_ValidCart_ReturnsOk() throws Exception {
        CartTotalDTO cartTotalDTO = new CartTotalDTO();
        cartTotalDTO.setSubtotal(new BigDecimal("199.98"));
        cartTotalDTO.setTax(new BigDecimal("20.00"));
        cartTotalDTO.setShipping(new BigDecimal("10.00"));
        cartTotalDTO.setTotal(new BigDecimal("229.98"));

        when(cartService.calculateCartTotal(userId)).thenReturn(cartTotalDTO);

        mockMvc.perform(get("/api/v1/carts/{userId}/total", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(199.98))
                .andExpect(jsonPath("$.tax").value(20.00))
                .andExpect(jsonPath("$.shipping").value(10.00))
                .andExpect(jsonPath("$.total").value(229.98));

        verify(cartService, times(1)).calculateCartTotal(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/carts/{userId} - Admin Access - Success")
    void testGetCart_AdminAccess_ReturnsOk() throws Exception {
        when(cartService.getCartByUserId(userId)).thenReturn(cartResponseDTO);

        mockMvc.perform(get("/api/v1/carts/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId));

        verify(cartService, times(1)).getCartByUserId(userId);
    }
}