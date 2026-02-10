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
 * JUnit 5 test class for CartController.
 * Tests all REST endpoints for cart management operations.
 * Uses MockMvc for controller testing and Mockito for service layer mocking.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@WebMvcTest(CartController.class)
@DisplayName("CartController Test Suite")
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
     * Set up test data before each test execution.
     * Initializes test UUIDs, CartDTO, and AddItemRequest objects.
     */
    @BeforeEach
    public void setUp() {
        testCartId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testItemId = UUID.randomUUID();

        testCartDTO = new CartDTO();
        testCartDTO.setCartId(testCartId);
        testCartDTO.setUserId(testUserId);
        testCartDTO.setItems(new ArrayList<>());
        testCartDTO.setTotalAmount(BigDecimal.ZERO);

        testAddItemRequest = new AddItemRequest();
        testAddItemRequest.setProductId(UUID.randomUUID());
        testAddItemRequest.setQuantity(2);
        testAddItemRequest.setPrice(BigDecimal.valueOf(29.99));
    }

    /**
     * Test adding an item to cart with valid request.
     * Verifies that the endpoint returns HTTP 200 and the correct CartDTO.
     *
     * @throws Exception if the request fails
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
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).addItemToCart(eq(testCartId), any(AddItemRequest.class));
    }

    /**
     * Test adding an item to cart with invalid request (missing required fields).
     * Verifies that the endpoint returns HTTP 400 Bad Request.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should return bad request when adding item with invalid data")
    public void testAddItemToCart_InvalidRequest() throws Exception {
        AddItemRequest invalidRequest = new AddItemRequest();

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test removing an item from cart with valid IDs.
     * Verifies that the endpoint returns HTTP 200 and the updated CartDTO.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should successfully remove item from cart")
    public void testRemoveItemFromCart_Success() throws Exception {
        when(cartService.removeItemFromCart(testCartId, testItemId))
                .thenReturn(testCartDTO);

        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()));

        verify(cartService, times(1)).removeItemFromCart(testCartId, testItemId);
    }

    /**
     * Test removing a non-existent item from cart.
     * Verifies proper error handling when item is not found.
     *
     * @throws Exception if the request fails
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
     * Test viewing cart with valid cart ID.
     * Verifies that the endpoint returns HTTP 200 and the correct CartDTO.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should successfully view cart")
    public void testViewCart_Success() throws Exception {
        when(cartService.viewCart(testCartId)).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/carts/{cartId}", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).viewCart(testCartId);
    }

    /**
     * Test viewing a non-existent cart.
     * Verifies proper error handling when cart is not found.
     *
     * @throws Exception if the request fails
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
     * Test cleaning up cart (removing all items) with valid cart ID.
     * Verifies that the endpoint returns HTTP 200 and an empty CartDTO.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should successfully cleanup cart")
    public void testCleanupCart_Success() throws Exception {
        CartDTO emptyCart = new CartDTO();
        emptyCart.setCartId(testCartId);
        emptyCart.setUserId(testUserId);
        emptyCart.setItems(new ArrayList<>());
        emptyCart.setTotalAmount(BigDecimal.ZERO);

        when(cartService.cleanupCart(testCartId)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/carts/{cartId}/items", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).cleanupCart(testCartId);
    }

    /**
     * Test getting or creating cart for a user.
     * Verifies that the endpoint returns HTTP 200 and the CartDTO.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should successfully get or create cart for user")
    public void testGetOrCreateCart_Success() throws Exception {
        when(cartService.getOrCreateCart(testUserId)).thenReturn(testCartDTO);

        mockMvc.perform(post("/api/carts/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).getOrCreateCart(testUserId);
    }

    /**
     * Test getting or creating cart for a new user (lazy creation).
     * Verifies that a new cart is created when user doesn't have one.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should create new cart for new user")
    public void testGetOrCreateCart_NewUser() throws Exception {
        UUID newUserId = UUID.randomUUID();
        CartDTO newCartDTO = new CartDTO();
        newCartDTO.setCartId(UUID.randomUUID());
        newCartDTO.setUserId(newUserId);
        newCartDTO.setItems(new ArrayList<>());
        newCartDTO.setTotalAmount(BigDecimal.ZERO);

        when(cartService.getOrCreateCart(newUserId)).thenReturn(newCartDTO);

        mockMvc.perform(post("/api/carts/user/{userId}", newUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(newUserId.toString()));

        verify(cartService, times(1)).getOrCreateCart(newUserId);
    }

    /**
     * Test logout cleanup for a user.
     * Verifies that the endpoint returns HTTP 204 No Content.
     *
     * @throws Exception if the request fails
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
     * Test logout cleanup with invalid user ID.
     * Verifies proper error handling during logout cleanup.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should handle logout cleanup errors gracefully")
    public void testLogoutCleanup_Error() throws Exception {
        UUID invalidUserId = UUID.randomUUID();
        doThrow(new RuntimeException("User not found")).when(cartService).logoutCleanup(invalidUserId);

        mockMvc.perform(post("/api/carts/logout/{userId}", invalidUserId))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).logoutCleanup(invalidUserId);
    }

    /**
     * Test adding item to cart with null cart ID.
     * Verifies proper validation of path variables.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should handle invalid UUID format in path variable")
    public void testAddItemToCart_InvalidUUID() throws Exception {
        mockMvc.perform(post("/api/carts/{cartId}/items", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddItemRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test adding item with zero quantity.
     * Verifies validation of business rules in the request.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should validate quantity in add item request")
    public void testAddItemToCart_ZeroQuantity() throws Exception {
        AddItemRequest zeroQuantityRequest = new AddItemRequest();
        zeroQuantityRequest.setProductId(UUID.randomUUID());
        zeroQuantityRequest.setQuantity(0);
        zeroQuantityRequest.setPrice(BigDecimal.valueOf(29.99));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroQuantityRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test adding item with negative price.
     * Verifies validation of price field in the request.
     *
     * @throws Exception if the request fails
     */
    @Test
    @DisplayName("Should validate price in add item request")
    public void testAddItemToCart_NegativePrice() throws Exception {
        AddItemRequest negativePriceRequest = new AddItemRequest();
        negativePriceRequest.setProductId(UUID.randomUUID());
        negativePriceRequest.setQuantity(2);
        negativePriceRequest.setPrice(BigDecimal.valueOf(-10.00));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativePriceRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }
}