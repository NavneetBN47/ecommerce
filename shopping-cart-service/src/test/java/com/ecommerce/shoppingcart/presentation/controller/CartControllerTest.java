package com.ecommerce.shoppingcart.presentation.controller;

import com.ecommerce.shoppingcart.application.service.CartService;
import com.ecommerce.shoppingcart.presentation.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private UUID testUserId;
    private UUID testCartId;
    private UUID testItemId;
    private UUID testProductId;
    private CartResponseDTO cartResponseDTO;
    private AddCartItemDTO addCartItemDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCartId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(testItemId);
        cartItemDTO.setProductId(testProductId);
        cartItemDTO.setProductName("Test Product");
        cartItemDTO.setQuantity(2);
        cartItemDTO.setPrice(new BigDecimal("99.99"));
        cartItemDTO.setSubtotal(new BigDecimal("199.98"));

        cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setId(testCartId);
        cartResponseDTO.setUserId(testUserId);
        cartResponseDTO.setItems(Arrays.asList(cartItemDTO));
        cartResponseDTO.setTotalAmount(new BigDecimal("199.98"));
        cartResponseDTO.setCreatedAt(LocalDateTime.now());
        cartResponseDTO.setUpdatedAt(LocalDateTime.now());

        addCartItemDTO = new AddCartItemDTO();
        addCartItemDTO.setProductId(testProductId);
        addCartItemDTO.setQuantity(2);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/cart/{userId} - Success")
    void testGetCart_Success() throws Exception {
        when(cartService.getCartByUserId(testUserId)).thenReturn(cartResponseDTO);

        mockMvc.perform(get("/api/v1/cart/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(199.98));

        verify(cartService, times(1)).getCartByUserId(testUserId);
    }

    @Test
    @DisplayName("GET /api/v1/cart/{userId} - Unauthorized")
    void testGetCart_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cart/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).getCartByUserId(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/cart/{userId} - Cart Not Found")
    void testGetCart_NotFound() throws Exception {
        when(cartService.getCartByUserId(testUserId))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/api/v1/cart/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).getCartByUserId(testUserId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/items - Success")
    void testAddItemToCart_Success() throws Exception {
        when(cartService.addItemToCart(eq(testUserId), any(AddCartItemDTO.class)))
                .thenReturn(cartResponseDTO);

        mockMvc.perform(post("/api/v1/cart/{userId}/items", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].productId").value(testProductId.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(cartService, times(1)).addItemToCart(eq(testUserId), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/items - Invalid Quantity")
    void testAddItemToCart_InvalidQuantity() throws Exception {
        AddCartItemDTO invalidDTO = new AddCartItemDTO();
        invalidDTO.setProductId(testProductId);
        invalidDTO.setQuantity(0);

        mockMvc.perform(post("/api/v1/cart/{userId}/items", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/items - Product Not Found")
    void testAddItemToCart_ProductNotFound() throws Exception {
        when(cartService.addItemToCart(eq(testUserId), any(AddCartItemDTO.class)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/api/v1/cart/{userId}/items", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).addItemToCart(eq(testUserId), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/items - Insufficient Stock")
    void testAddItemToCart_InsufficientStock() throws Exception {
        when(cartService.addItemToCart(eq(testUserId), any(AddCartItemDTO.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        mockMvc.perform(post("/api/v1/cart/{userId}/items", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCartItemDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addItemToCart(eq(testUserId), any(AddCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/v1/cart/{userId}/items/{itemId} - Success")
    void testUpdateCartItem_Success() throws Exception {
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(5);

        when(cartService.updateCartItem(eq(testUserId), eq(testItemId), any(UpdateCartItemDTO.class)))
                .thenReturn(cartResponseDTO);

        mockMvc.perform(put("/api/v1/cart/{userId}/items/{itemId}", testUserId, testItemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        verify(cartService, times(1)).updateCartItem(eq(testUserId), eq(testItemId), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/v1/cart/{userId}/items/{itemId} - Invalid Quantity")
    void testUpdateCartItem_InvalidQuantity() throws Exception {
        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setQuantity(-1);

        mockMvc.perform(put("/api/v1/cart/{userId}/items/{itemId}", testUserId, testItemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/cart/{userId}/items/{itemId} - Success")
    void testRemoveItemFromCart_Success() throws Exception {
        CartResponseDTO emptyCart = new CartResponseDTO();
        emptyCart.setId(testCartId);
        emptyCart.setUserId(testUserId);
        emptyCart.setItems(new ArrayList<>());
        emptyCart.setTotalAmount(BigDecimal.ZERO);

        when(cartService.removeItemFromCart(testUserId, testItemId)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/v1/cart/{userId}/items/{itemId}", testUserId, testItemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).removeItemFromCart(testUserId, testItemId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/cart/{userId}/items/{itemId} - Item Not Found")
    void testRemoveItemFromCart_NotFound() throws Exception {
        when(cartService.removeItemFromCart(testUserId, testItemId))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(delete("/api/v1/cart/{userId}/items/{itemId}", testUserId, testItemId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeItemFromCart(testUserId, testItemId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/cart/{userId} - Success")
    void testClearCart_Success() throws Exception {
        doNothing().when(cartService).clearCart(testUserId);

        mockMvc.perform(delete("/api/v1/cart/{userId}", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(testUserId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/checkout - Success")
    void testCheckout_Success() throws Exception {
        CheckoutRequestDTO checkoutDTO = new CheckoutRequestDTO();
        checkoutDTO.setShippingAddress("123 Main St");
        checkoutDTO.setPaymentMethod("CREDIT_CARD");

        CheckoutResponseDTO checkoutResponse = new CheckoutResponseDTO();
        checkoutResponse.setOrderId(UUID.randomUUID());
        checkoutResponse.setTotalAmount(new BigDecimal("199.98"));
        checkoutResponse.setStatus("CONFIRMED");

        when(cartService.checkout(eq(testUserId), any(CheckoutRequestDTO.class)))
                .thenReturn(checkoutResponse);

        mockMvc.perform(post("/api/v1/cart/{userId}/checkout", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(cartService, times(1)).checkout(eq(testUserId), any(CheckoutRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/checkout - Empty Cart")
    void testCheckout_EmptyCart() throws Exception {
        CheckoutRequestDTO checkoutDTO = new CheckoutRequestDTO();
        checkoutDTO.setShippingAddress("123 Main St");
        checkoutDTO.setPaymentMethod("CREDIT_CARD");

        when(cartService.checkout(eq(testUserId), any(CheckoutRequestDTO.class)))
                .thenThrow(new RuntimeException("Cart is empty"));

        mockMvc.perform(post("/api/v1/cart/{userId}/checkout", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutDTO)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).checkout(eq(testUserId), any(CheckoutRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/cart/{userId}/checkout - Insufficient Stock")
    void testCheckout_InsufficientStock() throws Exception {
        CheckoutRequestDTO checkoutDTO = new CheckoutRequestDTO();
        checkoutDTO.setShippingAddress("123 Main St");
        checkoutDTO.setPaymentMethod("CREDIT_CARD");

        when(cartService.checkout(eq(testUserId), any(CheckoutRequestDTO.class)))
                .thenThrow(new RuntimeException("Insufficient stock"));

        mockMvc.perform(post("/api/v1/cart/{userId}/checkout", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutDTO)))
                .andExpect(status().isConflict());

        verify(cartService, times(1)).checkout(eq(testUserId), any(CheckoutRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/cart/{userId}/total - Success")
    void testGetCartTotal_Success() throws Exception {
        CartTotalDTO totalDTO = new CartTotalDTO();
        totalDTO.setSubtotal(new BigDecimal("199.98"));
        totalDTO.setTax(new BigDecimal("20.00"));
        totalDTO.setTotal(new BigDecimal("219.98"));

        when(cartService.calculateCartTotal(testUserId)).thenReturn(totalDTO);

        mockMvc.perform(get("/api/v1/cart/{userId}/total", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(199.98))
                .andExpect(jsonPath("$.tax").value(20.00))
                .andExpect(jsonPath("$.total").value(219.98));

        verify(cartService, times(1)).calculateCartTotal(testUserId);
    }
}