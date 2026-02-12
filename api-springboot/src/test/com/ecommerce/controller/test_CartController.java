package com.ecommerce.controller;

import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * JUnit 5 test class for CartController.
 * Tests all REST endpoints for cart management operations including
 * adding items, removing items, viewing cart, cleanup, and user cart operations.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@WebMvcTest(CartController.class)
@DisplayName("CartController Test Suite")
class test_CartController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testCartId;
    private UUID testUserId;
    private UUID testItemId;
    private UUID testProductId;
    private CartDTO testCartDTO;
    private AddItemRequest testAddItemRequest;

    /**
     * Set up test data before each test execution.
     * Initializes common test objects and DTOs.
     */
    @BeforeEach
    void setUp() {
        testCartId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        // Initialize test CartDTO
        testCartDTO = new CartDTO();
        testCartDTO.setCartId(testCartId);
        testCartDTO.setUserId(testUserId);
        testCartDTO.setCartStatus("ACTIVE");
        testCartDTO.setTotalAmount(BigDecimal.valueOf(100.00));
        testCartDTO.setTotalItems(2);
        testCartDTO.setCurrencyCode("USD");
        testCartDTO.setCreatedAt(LocalDateTime.now());
        testCartDTO.setUpdatedAt(LocalDateTime.now());
        testCartDTO.setExpiresAt(LocalDateTime.now().plusDays(30));

        CartItemDTO itemDTO = new CartItemDTO();
        itemDTO.setItemId(testItemId);
        itemDTO.setProductId(testProductId);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(BigDecimal.valueOf(50.00));
        itemDTO.setTotalPrice(BigDecimal.valueOf(100.00));
        itemDTO.setProductName("Test Product");
        itemDTO.setAddedAt(LocalDateTime.now());

        List<CartItemDTO> items = new ArrayList<>();
        items.add(itemDTO);
        testCartDTO.setItems(items);

        // Initialize test AddItemRequest
        testAddItemRequest = new AddItemRequest();
        testAddItemRequest.setProductId(testProductId);
        testAddItemRequest.setQuantity(2);
    }

    /**
     * Test successful addition of item to cart.
     * Verifies that POST /api/carts/{cartId}/items returns 200 OK
     * with the updated cart DTO.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully add item to cart")
    void testAddItemToCart_Success() throws Exception {
        when(cartService.addItemToCart(eq(testCartId), any(AddItemRequest.class)))
                .thenReturn(testCartDTO);

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(cartService, times(1)).addItemToCart(eq(testCartId), any(AddItemRequest.class));
    }

    /**
     * Test adding item to cart with invalid request body.
     * Verifies that validation errors are properly handled.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should return 400 when adding item with invalid request")
    void testAddItemToCart_InvalidRequest() throws Exception {
        AddItemRequest invalidRequest = new AddItemRequest();
        // Missing required fields

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    /**
     * Test adding item to cart with zero quantity.
     * Verifies that validation rejects invalid quantity values.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should return 400 when adding item with zero quantity")
    void testAddItemToCart_ZeroQuantity() throws Exception {
        testAddItemRequest.setQuantity(0);

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    /**
     * Test successful removal of item from cart.
     * Verifies that DELETE /api/carts/{cartId}/items/{itemId} returns 200 OK
     * with the updated cart DTO.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully remove item from cart")
    void testRemoveItemFromCart_Success() throws Exception {
        CartDTO emptyCart = new CartDTO();
        emptyCart.setCartId(testCartId);
        emptyCart.setUserId(testUserId);
        emptyCart.setTotalAmount(BigDecimal.ZERO);
        emptyCart.setTotalItems(0);
        emptyCart.setItems(new ArrayList<>());

        when(cartService.removeItemFromCart(testCartId, testItemId))
                .thenReturn(emptyCart);

        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items", hasSize(0)));

        verify(cartService, times(1)).removeItemFromCart(testCartId, testItemId);
    }

    /**
     * Test removing non-existent item from cart.
     * Verifies that appropriate error is returned when item doesn't exist.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle removal of non-existent item")
    void testRemoveItemFromCart_ItemNotFound() throws Exception {
        UUID nonExistentItemId = UUID.randomUUID();
        when(cartService.removeItemFromCart(testCartId, nonExistentItemId))
                .thenThrow(new RuntimeException("Cart item not found"));

        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, nonExistentItemId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).removeItemFromCart(testCartId, nonExistentItemId);
    }

    /**
     * Test successful cart viewing.
     * Verifies that GET /api/carts/{cartId} returns 200 OK
     * with the complete cart DTO including all items.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully view cart")
    void testViewCart_Success() throws Exception {
        when(cartService.viewCart(testCartId)).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/carts/{cartId}", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.cartStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(cartService, times(1)).viewCart(testCartId);
    }

    /**
     * Test viewing non-existent cart.
     * Verifies that appropriate error is returned when cart doesn't exist.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle viewing non-existent cart")
    void testViewCart_CartNotFound() throws Exception {
        UUID nonExistentCartId = UUID.randomUUID();
        when(cartService.viewCart(nonExistentCartId))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/api/carts/{cartId}", nonExistentCartId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).viewCart(nonExistentCartId);
    }

    /**
     * Test successful cart cleanup.
     * Verifies that DELETE /api/carts/{cartId}/items removes all items
     * and returns 200 OK with empty cart.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully cleanup cart")
    void testCleanupCart_Success() throws Exception {
        CartDTO cleanedCart = new CartDTO();
        cleanedCart.setCartId(testCartId);
        cleanedCart.setUserId(testUserId);
        cleanedCart.setTotalAmount(BigDecimal.ZERO);
        cleanedCart.setTotalItems(0);
        cleanedCart.setItems(new ArrayList<>());

        when(cartService.cleanupCart(testCartId)).thenReturn(cleanedCart);

        mockMvc.perform(delete("/api/carts/{cartId}/items", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items", hasSize(0)));

        verify(cartService, times(1)).cleanupCart(testCartId);
    }

    /**
     * Test cleanup of already empty cart.
     * Verifies that cleanup operation is idempotent.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle cleanup of already empty cart")
    void testCleanupCart_AlreadyEmpty() throws Exception {
        CartDTO emptyCart = new CartDTO();
        emptyCart.setCartId(testCartId);
        emptyCart.setTotalAmount(BigDecimal.ZERO);
        emptyCart.setTotalItems(0);
        emptyCart.setItems(new ArrayList<>());

        when(cartService.cleanupCart(testCartId)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/carts/{cartId}/items", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).cleanupCart(testCartId);
    }

    /**
     * Test successful get or create cart for user.
     * Verifies that POST /api/carts/user/{userId} returns 200 OK
     * with existing or newly created cart.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully get or create cart for user")
    void testGetOrCreateCart_Success() throws Exception {
        when(cartService.getOrCreateCart(testUserId)).thenReturn(testCartDTO);

        mockMvc.perform(post("/api/carts/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.cartStatus").value("ACTIVE"));

        verify(cartService, times(1)).getOrCreateCart(testUserId);
    }

    /**
     * Test get or create cart for new user.
     * Verifies that a new cart is created when user has no active cart.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should create new cart for user without active cart")
    void testGetOrCreateCart_NewCart() throws Exception {
        UUID newUserId = UUID.randomUUID();
        CartDTO newCart = new CartDTO();
        newCart.setCartId(UUID.randomUUID());
        newCart.setUserId(newUserId);
        newCart.setCartStatus("ACTIVE");
        newCart.setTotalAmount(BigDecimal.ZERO);
        newCart.setTotalItems(0);
        newCart.setItems(new ArrayList<>());

        when(cartService.getOrCreateCart(newUserId)).thenReturn(newCart);

        mockMvc.perform(post("/api/carts/user/{userId}", newUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(newUserId.toString()))
                .andExpect(jsonPath("$.cartStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).getOrCreateCart(newUserId);
    }

    /**
     * Test get or create cart with invalid user ID.
     * Verifies that appropriate error is returned for non-existent user.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle get or create cart for non-existent user")
    void testGetOrCreateCart_UserNotFound() throws Exception {
        UUID invalidUserId = UUID.randomUUID();
        when(cartService.getOrCreateCart(invalidUserId))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/carts/user/{userId}", invalidUserId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).getOrCreateCart(invalidUserId);
    }

    /**
     * Test successful logout cleanup.
     * Verifies that POST /api/carts/logout/{userId} returns 204 No Content
     * after performing cleanup operations.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully perform logout cleanup")
    void testLogoutCleanup_Success() throws Exception {
        doNothing().when(cartService).logoutCleanup(testUserId);

        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).logoutCleanup(testUserId);
    }

    /**
     * Test logout cleanup with multiple active carts.
     * Verifies that all active carts are properly handled during logout.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle logout cleanup with multiple carts")
    void testLogoutCleanup_MultipleCarts() throws Exception {
        doNothing().when(cartService).logoutCleanup(testUserId);

        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).logoutCleanup(testUserId);
    }

    /**
     * Test logout cleanup for user with no active carts.
     * Verifies that cleanup is idempotent and handles empty state.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle logout cleanup for user with no carts")
    void testLogoutCleanup_NoActiveCarts() throws Exception {
        UUID userWithNoCarts = UUID.randomUUID();
        doNothing().when(cartService).logoutCleanup(userWithNoCarts);

        mockMvc.perform(post("/api/carts/logout/{userId}", userWithNoCarts))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).logoutCleanup(userWithNoCarts);
    }
}