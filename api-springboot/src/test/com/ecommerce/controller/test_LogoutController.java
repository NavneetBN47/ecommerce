package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for LogoutController
 * Tests logout operations and cart clearing functionality
 */
@WebMvcTest(LogoutController.class)
@DisplayName("LogoutController Tests")
class test_LogoutController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    private UUID testUserId;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    /**
     * Test successful logout
     * Verifies that logout clears cart and returns success response
     */
    @Test
    @DisplayName("Should logout successfully and clear cart")
    void testLogout_Success() throws Exception {
        doNothing().when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout without user ID header
     * Verifies that missing user ID header is handled
     */
    @Test
    @DisplayName("Should return 400 when X-User-Id header is missing")
    void testLogout_MissingUserId() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).clearCart(any(UUID.class));
    }

    /**
     * Test logout with invalid user ID format
     * Verifies that invalid UUID format is handled
     */
    @Test
    @DisplayName("Should return 400 when user ID format is invalid")
    void testLogout_InvalidUserIdFormat() throws Exception {
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).clearCart(any(UUID.class));
    }

    /**
     * Test logout with service exception
     * Verifies that service layer exceptions are properly handled
     */
    @Test
    @DisplayName("Should handle service exception during logout")
    void testLogout_ServiceException() throws Exception {
        doThrow(new RuntimeException("Service error")).when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout with empty user ID
     * Verifies that empty user ID is handled
     */
    @Test
    @DisplayName("Should return 400 when user ID is empty")
    void testLogout_EmptyUserId() throws Exception {
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", ""))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).clearCart(any(UUID.class));
    }

    /**
     * Test logout for non-existent user
     * Verifies that logout succeeds even for non-existent users
     */
    @Test
    @DisplayName("Should succeed logout for non-existent user")
    void testLogout_NonExistentUser() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        doNothing().when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", nonExistentUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(cartService, times(1)).clearCart(nonExistentUserId);
    }

    /**
     * Test multiple logout calls
     * Verifies that multiple logout calls are idempotent
     */
    @Test
    @DisplayName("Should handle multiple logout calls idempotently")
    void testLogout_MultipleCallsIdempotent() throws Exception {
        doNothing().when(cartService).clearCart(any(UUID.class));

        // First logout
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk());

        // Second logout
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk());

        verify(cartService, times(2)).clearCart(testUserId);
    }

    /**
     * Test logout with null pointer exception in service
     * Verifies that null pointer exceptions are handled
     */
    @Test
    @DisplayName("Should handle null pointer exception in service")
    void testLogout_NullPointerException() throws Exception {
        doThrow(new NullPointerException("Null cart")).when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().is5xxServerError());

        verify(cartService, times(1)).clearCart(testUserId);
    }
}