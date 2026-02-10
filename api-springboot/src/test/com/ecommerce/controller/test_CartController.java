package com.ecommerce.controller;

import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
 * Tests all public REST endpoints for cart management operations.
 * Uses Mockito to mock the CartService layer.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Test Suite")
class test_CartController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID testCartId;
    private UUID testUserId;
    private UUID testItemId;
    private CartDTO testCartDTO;
    private AddItemRequest testAddItemRequest;

    /**
     * Set up test fixtures before each test method.
     * Initializes MockMvc, test data, and common objects.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();
        
        testCartId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        
        // Initialize test CartDTO
        testCartDTO = new CartDTO();
        testCartDTO.setCartId(testCartId);
        testCartDTO.setUserId(testUserId);
        testCartDTO.setItems(new ArrayList<>());
        testCartDTO.setTotalPrice(BigDecimal.ZERO);
        
        // Initialize test AddItemRequest
        testAddItemRequest = new AddItemRequest();
        testAddItemRequest.setProductId(UUID.randomUUID());
        testAddItemRequest.setQuantity(2);
    }

    /**
     * Test successful addition of item to cart.
     * Verifies that the endpoint returns 200 OK and the correct CartDTO.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully add item to cart")
    void testAddItemToCart_Success() throws Exception {
        // Arrange
        when(cartService.addItemToCart(eq(testCartId), any(AddItemRequest.class)))
            .thenReturn(testCartDTO);

        // Act & Assert
        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAddItemRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
            .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).addItemToCart(eq(testCartId), any(AddItemRequest.class));
    }

    /**
     * Test adding item to cart with invalid request body.
     * Verifies that validation errors are handled properly.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should return 400 when adding item with invalid request")
    void testAddItemToCart_InvalidRequest() throws Exception {
        // Arrange
        AddItemRequest invalidRequest = new AddItemRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test successful removal of item from cart.
     * Verifies that the endpoint returns 200 OK and updated CartDTO.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully remove item from cart")
    void testRemoveItemFromCart_Success() throws Exception {
        // Arrange
        when(cartService.removeItemFromCart(testCartId, testItemId))
            .thenReturn(testCartDTO);

        // Act & Assert
        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, testItemId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cartId").value(testCartId.toString()));

        verify(cartService, times(1)).removeItemFromCart(testCartId, testItemId);
    }

    /**
     * Test removing non-existent item from cart.
     * Verifies that appropriate error handling occurs.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle removal of non-existent item")
    void testRemoveItemFromCart_ItemNotFound() throws Exception {
        // Arrange
        UUID nonExistentItemId = UUID.randomUUID();
        when(cartService.removeItemFromCart(testCartId, nonExistentItemId))
            .thenThrow(new RuntimeException("Item not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, nonExistentItemId))
            .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).removeItemFromCart(testCartId, nonExistentItemId);
    }

    /**
     * Test successful cart viewing.
     * Verifies that the endpoint returns 200 OK and the correct CartDTO.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully view cart")
    void testViewCart_Success() throws Exception {
        // Arrange
        when(cartService.viewCart(testCartId)).thenReturn(testCartDTO);

        // Act & Assert
        mockMvc.perform(get("/api/carts/{cartId}", testCartId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
            .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).viewCart(testCartId);
    }

    /**
     * Test viewing non-existent cart.
     * Verifies that appropriate error is returned.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle viewing non-existent cart")
    void testViewCart_CartNotFound() throws Exception {
        // Arrange
        UUID nonExistentCartId = UUID.randomUUID();
        when(cartService.viewCart(nonExistentCartId))
            .thenThrow(new RuntimeException("Cart not found"));

        // Act & Assert
        mockMvc.perform(get("/api/carts/{cartId}", nonExistentCartId))
            .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).viewCart(nonExistentCartId);
    }

    /**
     * Test successful cart cleanup (removing all items).
     * Verifies that the endpoint returns 200 OK and empty cart.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully cleanup cart")
    void testCleanupCart_Success() throws Exception {
        // Arrange
        CartDTO emptyCart = new CartDTO();
        emptyCart.setCartId(testCartId);
        emptyCart.setUserId(testUserId);
        emptyCart.setItems(new ArrayList<>());
        emptyCart.setTotalPrice(BigDecimal.ZERO);
        
        when(cartService.cleanupCart(testCartId)).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(delete("/api/carts/{cartId}/items", testCartId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
            .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).cleanupCart(testCartId);
    }

    /**
     * Test cleanup of non-existent cart.
     * Verifies that appropriate error handling occurs.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle cleanup of non-existent cart")
    void testCleanupCart_CartNotFound() throws Exception {
        // Arrange
        UUID nonExistentCartId = UUID.randomUUID();
        when(cartService.cleanupCart(nonExistentCartId))
            .thenThrow(new RuntimeException("Cart not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/carts/{cartId}/items", nonExistentCartId))
            .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).cleanupCart(nonExistentCartId);
    }

    /**
     * Test successful get or create cart for user.
     * Verifies that the endpoint returns 200 OK and CartDTO.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully get or create cart for user")
    void testGetOrCreateCart_Success() throws Exception {
        // Arrange
        when(cartService.getOrCreateCart(testUserId)).thenReturn(testCartDTO);

        // Act & Assert
        mockMvc.perform(post("/api/carts/user/{userId}", testUserId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
            .andExpect(jsonPath("$.userId").value(testUserId.toString()));

        verify(cartService, times(1)).getOrCreateCart(testUserId);
    }

    /**
     * Test get or create cart with new user (lazy creation).
     * Verifies that a new cart is created for new users.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should create new cart for new user")
    void testGetOrCreateCart_NewUser() throws Exception {
        // Arrange
        UUID newUserId = UUID.randomUUID();
        UUID newCartId = UUID.randomUUID();
        CartDTO newCartDTO = new CartDTO();
        newCartDTO.setCartId(newCartId);
        newCartDTO.setUserId(newUserId);
        newCartDTO.setItems(new ArrayList<>());
        newCartDTO.setTotalPrice(BigDecimal.ZERO);
        
        when(cartService.getOrCreateCart(newUserId)).thenReturn(newCartDTO);

        // Act & Assert
        mockMvc.perform(post("/api/carts/user/{userId}", newUserId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cartId").value(newCartId.toString()))
            .andExpect(jsonPath("$.userId").value(newUserId.toString()));

        verify(cartService, times(1)).getOrCreateCart(newUserId);
    }

    /**
     * Test successful logout cleanup.
     * Verifies that the endpoint returns 204 No Content.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should successfully perform logout cleanup")
    void testLogoutCleanup_Success() throws Exception {
        // Arrange
        doNothing().when(cartService).logoutCleanup(testUserId);

        // Act & Assert
        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
            .andExpect(status().isNoContent());

        verify(cartService, times(1)).logoutCleanup(testUserId);
    }

    /**
     * Test logout cleanup with invalid user ID.
     * Verifies that appropriate error handling occurs.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle logout cleanup with invalid user")
    void testLogoutCleanup_InvalidUser() throws Exception {
        // Arrange
        UUID invalidUserId = UUID.randomUUID();
        doThrow(new RuntimeException("User not found"))
            .when(cartService).logoutCleanup(invalidUserId);

        // Act & Assert
        mockMvc.perform(post("/api/carts/logout/{userId}", invalidUserId))
            .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).logoutCleanup(invalidUserId);
    }

    /**
     * Test logout cleanup is idempotent.
     * Verifies that multiple logout calls don't cause issues.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle multiple logout cleanup calls")
    void testLogoutCleanup_Idempotent() throws Exception {
        // Arrange
        doNothing().when(cartService).logoutCleanup(testUserId);

        // Act & Assert - First call
        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
            .andExpect(status().isNoContent());

        // Act & Assert - Second call
        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
            .andExpect(status().isNoContent());

        verify(cartService, times(2)).logoutCleanup(testUserId);
    }

    /**
     * Test adding item with zero quantity.
     * Verifies that edge case validation works properly.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle adding item with zero quantity")
    void testAddItemToCart_ZeroQuantity() throws Exception {
        // Arrange
        AddItemRequest zeroQuantityRequest = new AddItemRequest();
        zeroQuantityRequest.setProductId(UUID.randomUUID());
        zeroQuantityRequest.setQuantity(0);

        // Act & Assert
        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroQuantityRequest)))
            .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }

    /**
     * Test adding item with negative quantity.
     * Verifies that validation rejects negative quantities.
     * 
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should handle adding item with negative quantity")
    void testAddItemToCart_NegativeQuantity() throws Exception {
        // Arrange
        AddItemRequest negativeQuantityRequest = new AddItemRequest();
        negativeQuantityRequest.setProductId(UUID.randomUUID());
        negativeQuantityRequest.setQuantity(-1);

        // Act & Assert
        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeQuantityRequest)))
            .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(UUID.class), any(AddItemRequest.class));
    }
}