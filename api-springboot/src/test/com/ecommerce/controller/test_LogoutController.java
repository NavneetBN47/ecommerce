package com.ecommerce.controller;

import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for LogoutController
 * Tests logout functionality and cart clearing operations
 */
@ExtendWith(MockitoExtension.class)
class test_LogoutController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private LogoutController logoutController;

    private MockMvc mockMvc;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logoutController).build();
        testUserId = UUID.randomUUID();
    }

    /**
     * Test successful logout
     * Verifies that logout request clears cart and returns success response
     */
    @Test
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
     * Verifies that missing user ID header is rejected
     */
    @Test
    void testLogout_MissingUserId() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).clearCart(any(UUID.class));
    }

    /**
     * Test logout with invalid user ID format
     * Verifies that invalid UUID format is rejected
     */
    @Test
    void testLogout_InvalidUserIdFormat() throws Exception {
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).clearCart(any(UUID.class));
    }

    /**
     * Test logout with service exception
     * Verifies proper error handling when cart service fails
     */
    @Test
    void testLogout_ServiceException() throws Exception {
        doThrow(new RuntimeException("Cart service error")).when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout with non-existent user
     * Verifies that logout succeeds even if user doesn't exist
     */
    @Test
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
     * Verifies that multiple logout calls are handled correctly
     */
    @Test
    void testLogout_MultipleCalls() throws Exception {
        doNothing().when(cartService).clearCart(any(UUID.class));

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk());

        verify(cartService, times(2)).clearCart(testUserId);
    }
}