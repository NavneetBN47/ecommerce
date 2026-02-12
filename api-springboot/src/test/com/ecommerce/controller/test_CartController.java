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
 * Tests all REST endpoints for cart management operations.
 * Mocks the CartService layer to isolate controller logic.
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
    private UUID testProductId;
    private CartDTO testCartDTO;
    private AddItemRequest testAddItemRequest;

    /**
     * Set up test data before each test execution.
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
        itemDTO.setProductName("Test Product");
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(BigDecimal.valueOf(50.00));
        itemDTO.setTotalPrice(BigDecimal.valueOf(100.00));
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
     * Verifies that POST /api/carts/{cartId}/items returns 200 OK with cart data.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should add item to cart successfully")
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
     * Test successful removal of item from cart.
     * Verifies that DELETE /api/carts/{cartId}/items/{itemId} returns 200 OK.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should remove item from cart successfully")
    void testRemoveItemFromCart_Success() throws Exception {
        CartDTO emptyCartDTO = new CartDTO();
        emptyCartDTO.setCartId(testCartId);
        emptyCartDTO.setUserId(testUserId);
        emptyCartDTO.setTotalAmount(BigDecimal.ZERO);
        emptyCartDTO.setTotalItems(0);
        emptyCartDTO.setItems(new ArrayList<>());

        when(cartService.removeItemFromCart(testCartId, testItemId))
                .thenReturn(emptyCartDTO);

        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).removeItemFromCart(testCartId, testItemId);
    }

    /**
     * Test viewing cart details.
     * Verifies that GET /api/carts/{cartId} returns cart with all items.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should view cart successfully")
    void testViewCart_Success() throws Exception {
        when(cartService.viewCart(testCartId)).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/carts/{cartId}", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.cartStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(cartService, times(1)).viewCart(testCartId);
    }

    /**
     * Test cleanup cart operation.
     * Verifies that DELETE /api/carts/{cartId}/items removes all items.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should cleanup cart successfully")
    void testCleanupCart_Success() throws Exception {
        CartDTO emptyCartDTO = new CartDTO();
        emptyCartDTO.setCartId(testCartId);
        emptyCartDTO.setUserId(testUserId);
        emptyCartDTO.setTotalAmount(BigDecimal.ZERO);
        emptyCartDTO.setTotalItems(0);
        emptyCartDTO.setItems(new ArrayList<>());

        when(cartService.cleanupCart(testCartId)).thenReturn(emptyCartDTO);

        mockMvc.perform(delete("/api/carts/{cartId}/items", testCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCartId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items", hasSize(0)));

        verify(cartService, times(1)).cleanupCart(testCartId);
    }

    /**
     * Test get or create cart for user.
     * Verifies that POST /api/carts/user/{userId} returns existing or new cart.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should get or create cart for user successfully")
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
     * Test get or create cart when user has no existing cart.
     * Verifies lazy cart creation functionality.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should create new cart when user has no existing cart")
    void testGetOrCreateCart_NewCart() throws Exception {
        CartDTO newCartDTO = new CartDTO();
        UUID newCartId = UUID.randomUUID();
        newCartDTO.setCartId(newCartId);
        newCartDTO.setUserId(testUserId);
        newCartDTO.setCartStatus("ACTIVE");
        newCartDTO.setTotalAmount(BigDecimal.ZERO);
        newCartDTO.setTotalItems(0);
        newCartDTO.setItems(new ArrayList<>());

        when(cartService.getOrCreateCart(testUserId)).thenReturn(newCartDTO);

        mockMvc.perform(post("/api/carts/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(newCartId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.totalItems").value(0));

        verify(cartService, times(1)).getOrCreateCart(testUserId);
    }

    /**
     * Test logout cleanup operation.
     * Verifies that POST /api/carts/logout/{userId} returns 204 No Content.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should perform logout cleanup successfully")
    void testLogoutCleanup_Success() throws Exception {
        doNothing().when(cartService).logoutCleanup(testUserId);

        mockMvc.perform(post("/api/carts/logout/{userId}", testUserId))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).logoutCleanup(testUserId);
    }

    /**
     * Test adding item to cart with zero quantity.
     * Verifies that validation rejects invalid quantity.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should reject adding item with zero quantity")
    void testAddItemToCart_ZeroQuantity() throws Exception {
        AddItemRequest invalidRequest = new AddItemRequest();
        invalidRequest.setProductId(testProductId);
        invalidRequest.setQuantity(0);

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    /**
     * Test adding item to cart with negative quantity.
     * Verifies that validation rejects negative quantity.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should reject adding item with negative quantity")
    void testAddItemToCart_NegativeQuantity() throws Exception {
        AddItemRequest invalidRequest = new AddItemRequest();
        invalidRequest.setProductId(testProductId);
        invalidRequest.setQuantity(-5);

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addItemToCart(any(), any());
    }

    /**
     * Test viewing cart with invalid UUID format.
     * Verifies proper error handling for malformed UUIDs.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should return 400 for invalid cart ID format")
    void testViewCart_InvalidUUID() throws Exception {
        mockMvc.perform(get("/api/carts/{cartId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).viewCart(any());
    }

    /**
     * Test removing item with invalid item ID format.
     * Verifies proper error handling for malformed UUIDs.
     *
     * @throws Exception if request fails
     */
    @Test
    @DisplayName("Should return 400 for invalid item ID format")
    void testRemoveItemFromCart_InvalidUUID() throws Exception {
        mockMvc.perform(delete("/api/carts/{cartId}/items/{itemId}", testCartId, "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).removeItemFromCart(any(), any());
    }
}