package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for CartController
 * Tests cart operations including add, update, remove, and retrieve cart items
 */
@ExtendWith(MockitoExtension.class)
class test_CartController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID testUserId;
    private UUID testItemId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
        testUserId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
    }

    /**
     * Test adding product to cart successfully
     * Verifies that a valid add to cart request returns HTTP 201 with cart response
     */
    @Test
    void testAddProductToCart_Success() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(2);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserId(testUserId);
        cartResponse.setTotalAmount(BigDecimal.valueOf(100.00));

        when(cartService.addProductToCart(any(UUID.class), any(AddToCartRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(post("/api/cart/items")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).addProductToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    /**
     * Test adding product with invalid quantity
     * Verifies that invalid quantity is rejected
     */
    @Test
    void testAddProductToCart_InvalidQuantity() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(-1);

        mockMvc.perform(post("/api/cart/items")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addProductToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    /**
     * Test adding product without user ID header
     * Verifies that missing user ID header is rejected
     */
    @Test
    void testAddProductToCart_MissingUserId() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(2);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addProductToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    /**
     * Test updating cart item successfully
     * Verifies that a valid update request returns HTTP 200 with updated cart
     */
    @Test
    void testUpdateCartItem_Success() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserId(testUserId);
        cartResponse.setTotalAmount(BigDecimal.valueOf(250.00));

        when(cartService.updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(put("/api/cart/items/" + testItemId)
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class));
    }

    /**
     * Test updating cart item with zero quantity
     * Verifies that zero quantity update is rejected
     */
    @Test
    void testUpdateCartItem_ZeroQuantity() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        mockMvc.perform(put("/api/cart/items/" + testItemId)
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(any(UUID.class), any(UUID.class), any(UpdateCartItemRequest.class));
    }

    /**
     * Test removing cart item successfully
     * Verifies that a valid remove request returns HTTP 200 with updated cart
     */
    @Test
    void testRemoveCartItem_Success() throws Exception {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserId(testUserId);
        cartResponse.setTotalAmount(BigDecimal.valueOf(50.00));

        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(delete("/api/cart/items/" + testItemId)
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).removeCartItem(any(UUID.class), any(UUID.class));
    }

    /**
     * Test removing cart item that results in empty cart
     * Verifies that removing last item returns HTTP 204 No Content
     */
    @Test
    void testRemoveCartItem_EmptyCart() throws Exception {
        when(cartService.removeCartItem(any(UUID.class), any(UUID.class)))
                .thenReturn(null);

        mockMvc.perform(delete("/api/cart/items/" + testItemId)
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeCartItem(any(UUID.class), any(UUID.class));
    }

    /**
     * Test getting cart successfully
     * Verifies that get cart request returns HTTP 200 with cart data
     */
    @Test
    void testGetCart_Success() throws Exception {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserId(testUserId);
        cartResponse.setTotalAmount(BigDecimal.valueOf(150.00));

        when(cartService.getCart(any(UUID.class))).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(150.00));

        verify(cartService, times(1)).getCart(any(UUID.class));
    }

    /**
     * Test getting cart without user ID
     * Verifies that missing user ID header is rejected
     */
    @Test
    void testGetCart_MissingUserId() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).getCart(any(UUID.class));
    }

    /**
     * Test logout (clear cart) successfully
     * Verifies that logout request clears cart and returns HTTP 200
     */
    @Test
    void testLogout_Success() throws Exception {
        doNothing().when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/cart/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(any(UUID.class));
    }

    /**
     * Test logout with service exception
     * Verifies proper error handling when clear cart fails
     */
    @Test
    void testLogout_ServiceException() throws Exception {
        doThrow(new RuntimeException("Service error")).when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/cart/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).clearCart(any(UUID.class));
    }
}