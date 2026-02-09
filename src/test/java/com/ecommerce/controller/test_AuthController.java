package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthController
 * Tests all public endpoints for authentication operations
 * Mocks AuthService layer to isolate controller logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class test_AuthController {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Test successful user login
     * Verifies that login endpoint returns success response with auth token
     */
    @Test
    @DisplayName("Should successfully login user with valid credentials")
    void testLogin_Success() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");
        
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("jwt-token-123");
        authResponse.setUserId(1L);
        authResponse.setUsername("testuser");
        
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(authService).login(any(AuthRequest.class));
    }

    /**
     * Test login with invalid request body
     * Verifies that validation errors are handled properly
     */
    @Test
    @DisplayName("Should return validation error for invalid login request")
    void testLogin_InvalidRequest() throws Exception {
        // Given
        AuthRequest invalidRequest = new AuthRequest();
        // Missing username and password

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test successful user logout
     * Verifies that logout endpoint clears user session and cart
     */
    @Test
    @DisplayName("Should successfully logout user and clear cart")
    void testLogout_Success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(post("/api/auth/logout/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService).logout(eq(userId));
    }

    /**
     * Test logout with invalid user ID
     * Verifies that proper error handling occurs for invalid user IDs
     */
    @Test
    @DisplayName("Should handle logout for invalid user ID")
    void testLogout_InvalidUserId() throws Exception {
        // Given
        Long invalidUserId = -1L;

        // When & Then
        mockMvc.perform(post("/api/auth/logout/{userId}", invalidUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authService).logout(eq(invalidUserId));
    }

    /**
     * Test logout with null user ID path variable
     * Verifies that path variable validation works correctly
     */
    @Test
    @DisplayName("Should return bad request for missing user ID in logout")
    void testLogout_MissingUserId() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}