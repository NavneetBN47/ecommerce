package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for LogoutController
 * Tests logout endpoint with cart cleanup functionality
 * Mocks CartService layer to isolate controller logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutController Tests")
class test_LogoutController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private LogoutController logoutController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logoutController).build();
    }

    /**
     * Test successful logout with valid user ID
     * Verifies that logout clears user cart and returns success response
     */
    @Test
    @DisplayName("Should successfully logout user and clear cart")
    void testLogout_Success() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(cartService).clearCart(userId);
    }

    /**
     * Test logout without user ID header
     * Verifies that missing header is handled properly
     */
    @Test
    @DisplayName("Should return bad request when user ID header is missing")
    void testLogout_MissingUserIdHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test logout with invalid UUID format
     * Verifies that malformed UUID is handled properly
     */
    @Test
    @DisplayName("Should return bad request for invalid UUID format")
    void testLogout_InvalidUuidFormat() throws Exception {
        // Given
        String invalidUuid = "invalid-uuid-format";

        // When & Then
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", invalidUuid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test logout when cart service throws exception
     * Verifies that service layer exceptions are handled properly
     */
    @Test
    @DisplayName("Should handle cart service exceptions during logout")
    void testLogout_CartServiceException() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        
        // Mock cart service to throw exception
        org.mockito.Mockito.doThrow(new RuntimeException("Cart service error"))
                .when(cartService).clearCart(any(UUID.class));

        // When & Then
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(cartService).clearCart(userId);
    }

    /**
     * Test logout with empty user ID header
     * Verifies that empty header value is handled properly
     */
    @Test
    @DisplayName("Should return bad request for empty user ID header")
    void testLogout_EmptyUserIdHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test logout with null UUID
     * Verifies that null UUID is handled properly
     */
    @Test
    @DisplayName("Should handle null UUID in logout")
    void testLogout_NullUuid() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/logout")
                .header("X-User-Id", "null")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}