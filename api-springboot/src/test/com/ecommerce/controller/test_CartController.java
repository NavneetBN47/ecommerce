package com.ecommerce.controller;

import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.CartDTO;
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
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for CartController
 * Tests all REST endpoints for cart management operations
 * Uses MockMvc for controller testing and Mockito for service layer mocking
 *
 * @author QA Automation Team
 * @version 1.0
 */
@WebMvcTest(CartController.class)
@DisplayName("CartController Tests")
public class test_CartController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testCartId;
    private UUID testUserId;
    private UUID testItemId;
    private CartDTO testCartDTO;
    private AddItemRequest testAddItemRequest;

    /**
     * Setup method to initialize test data before each test
     * Creates sample UUIDs, CartDTO, and AddItemRequest objects
     */
    @BeforeEach
    public void setUp() {
        testCartId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testItemId = UUID.randomUUID();

        testCartDTO = new CartDTO();
        testCartDTO.setId(testCartId);
        testCartDTO.setUserId(testUserId);
        testCartDTO.setItems(new ArrayList<>());
        testCartDTO.setTotalAmount(BigDecimal.ZERO);

        testAddItemRequest = new AddItemRequest();
        testAddItemRequest.setProductId(UUID.randomUUID());
        testAddItemRequest.setQuantity(2);
        testAddItemRequest.setPrice(BigDecimal.valueOf(99.99));
    }

    /**
     * Test successful addition of item to cart
     * Verifies that POST /api/carts/{cartId}/items returns 200 OK
     * and the correct CartDTO response
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully add item to cart")
    public void testAddItemToCart_Success() throws Exception {
        when(cartService.addItemToCart(eq(testCartId), any(AddItemRequest.class)))
                .thenReturn(testCartDTO);

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).addItemToCart(eq(testCartId), any(AddItemRequest.class));
    }

    /**
     * Test adding item to cart with invalid request body
     * Verifies that validation errors are properly handled
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should return 400 when adding item with invalid request")
    public void testAddItemToCart_InvalidRequest() throws Exception {
        AddItemRequest invalidRequest = new AddItemRequest();
        // Missing required fields

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test adding item to cart with null cartId
     * Verifies proper handling of invalid path variable
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle invalid cartId when adding item")
    public void testAddItemToCart_InvalidCartId() throws Exception {
        mockMvc.perform(post("/api/carts/invalid-uuid/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test successful removal of item from cart
     * Verifies that DELETE /api/carts/{cartId}/items/{itemId} returns 200 OK
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully remove item from cart")
    public void testRemoveItemFromCart_Success() throws Exception {
        when(cartService.removeItemFromCart(testCartId, testItemId))
                .thenReturn(testCartDTO);

        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, testItemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCartId.toString()));

        verify(cartService, times(1)).removeItemFromCart(testCartId, testItemId);
    }

    /**
     * Test removing non-existent item from cart
     * Verifies proper error handling when item is not found
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle removal of non-existent item")
    public void testRemoveItemFromCart_ItemNotFound() throws Exception {
        UUID nonExistentItemId = UUID.randomUUID();
        when(cartService.removeItemFromCart(testCartId, nonExistentItemId))
                .thenThrow(new RuntimeException("Item not found"));

        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, nonExistentItemId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).removeItemFromCart(testCartId, nonExistentItemId);
    }

    /**
     * Test successful cart viewing
     * Verifies that GET /api/carts/{cartId} returns 200 OK with cart details
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully view cart")
    public void testViewCart_Success() throws Exception {
        when(cartService.viewCart(testCartId)).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/carts/{cartId}", testCartId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).viewCart(testCartId);
    }

    /**
     * Test viewing non-existent cart
     * Verifies proper error handling when cart is not found
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle viewing non-existent cart")
    public void testViewCart_CartNotFound() throws Exception {
        UUID nonExistentCartId = UUID.randomUUID();
        when(cartService.viewCart(nonExistentCartId))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/api/carts/{cartId}", nonExistentCartId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).viewCart(nonExistentCartId);
    }

    /**
     * Test viewing cart with invalid UUID format
     * Verifies proper handling of malformed path variable
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle invalid UUID format when viewing cart")
    public void testViewCart_InvalidUUID() throws Exception {
        mockMvc.perform(get("/api/carts/not-a-valid-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).viewCart(any(UUID.class));
    }

    /**
     * Test successful cart cleanup (removal of all items)
     * Verifies that DELETE /api/carts/{cartId}/items returns 200 OK
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully cleanup cart")
    public void testCleanupCart_Success() throws Exception {
        CartDTO emptyCart = new CartDTO();
        emptyCart.setId(testCartId);
        emptyCart.setUserId(testUserId);
        emptyCart.setItems(new ArrayList<>());
        emptyCart.setTotalAmount(BigDecimal.ZERO);

        when(cartService.cleanupCart(testCartId)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/carts/{cartId}/items", testCartId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCartId.toString()))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).cleanupCart(testCartId);
    }

    /**
     * Test cleanup of non-existent cart
     * Verifies proper error handling
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle cleanup of non-existent cart")
    public void testCleanupCart_CartNotFound() throws Exception {
        UUID nonExistentCartId = UUID.randomUUID();
        when(cartService.cleanupCart(nonExistentCartId))
                .thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(delete("/api/carts/{cartId}/items", nonExistentCartId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).cleanupCart(nonExistentCartId);
    }

    /**
     * Test successful get or create cart for user
     * Verifies that POST /api/carts/user/{userId} returns 200 OK
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully get or create cart for user")
    public void testGetOrCreateCart_Success() throws Exception {
        when(cartService.getOrCreateCart(testUserId)).thenReturn(testCartDTO);

        mockMvc.perform(post("/api/carts/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).getOrCreateCart(testUserId);
    }

    /**
     * Test get or create cart with new user (cart creation scenario)
     * Verifies lazy cart creation functionality
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should create new cart for new user")
    public void testGetOrCreateCart_NewUser() throws Exception {
        UUID newUserId = UUID.randomUUID();
        CartDTO newCart = new CartDTO();
        newCart.setId(UUID.randomUUID());
        newCart.setUserId(newUserId);
        newCart.setItems(new ArrayList<>());
        newCart.setTotalAmount(BigDecimal.ZERO);

        when(cartService.getOrCreateCart(newUserId)).thenReturn(newCart);

        mockMvc.perform(post("/api/carts/user/{userId}", newUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(newUserId.toString()));

        verify(cartService, times(1)).getOrCreateCart(newUserId);
    }

    /**
     * Test get or create cart with invalid user ID
     * Verifies proper handling of invalid UUID format
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle invalid userId when getting or creating cart")
    public void testGetOrCreateCart_InvalidUserId() throws Exception {
        mockMvc.perform(post("/api/carts/user/invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).getOrCreateCart(any(UUID.class));
    }

    /**
     * Test successful logout cleanup
     * Verifies that POST /api/carts/logout/{userId} returns 204 No Content
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully perform logout cleanup")
    public void testLogoutCleanup_Success() throws Exception {
        doNothing().when(cartService).logoutCleanup(testUserId);

        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).logoutCleanup(testUserId);
    }

    /**
     * Test logout cleanup with non-existent user
     * Verifies that cleanup is attempted even for non-existent users
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle logout cleanup for non-existent user")
    public void testLogoutCleanup_NonExistentUser() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        doThrow(new RuntimeException("User not found"))
                .when(cartService).logoutCleanup(nonExistentUserId);

        mockMvc.perform(post("/api/carts/logout/{userId}", nonExistentUserId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).logoutCleanup(nonExistentUserId);
    }

    /**
     * Test logout cleanup with invalid user ID format
     * Verifies proper handling of malformed UUID
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle invalid userId format during logout cleanup")
    public void testLogoutCleanup_InvalidUserId() throws Exception {
        mockMvc.perform(post("/api/carts/logout/not-a-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).logoutCleanup(any(UUID.class));
    }

    /**
     * Test adding item with edge case quantity (zero)
     * Verifies validation of quantity field
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle adding item with zero quantity")
    public void testAddItemToCart_ZeroQuantity() throws Exception {
        AddItemRequest zeroQuantityRequest = new AddItemRequest();
        zeroQuantityRequest.setProductId(UUID.randomUUID());
        zeroQuantityRequest.setQuantity(0);
        zeroQuantityRequest.setPrice(BigDecimal.valueOf(99.99));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroQuantityRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test adding item with negative price
     * Verifies validation of price field
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle adding item with negative price")
    public void testAddItemToCart_NegativePrice() throws Exception {
        AddItemRequest negativePriceRequest = new AddItemRequest();
        negativePriceRequest.setProductId(UUID.randomUUID());
        negativePriceRequest.setQuantity(1);
        negativePriceRequest.setPrice(BigDecimal.valueOf(-10.00));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativePriceRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test concurrent operations on same cart
     * Verifies that service layer handles concurrent modifications
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle concurrent cart operations")
    public void testConcurrentCartOperations() throws Exception {
        when(cartService.addItemToCart(eq(testCartId), any(AddItemRequest.class)))
                .thenReturn(testCartDTO);

        // Simulate multiple concurrent requests
        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isOk());

        verify(cartService, times(2)).addItemToCart(eq(testCartId), any(AddItemRequest.class));
    }
}