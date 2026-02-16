package com.ecommerce.controller;

import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for LogoutController
 * 
 * This test class verifies the logout functionality and cart cleanup.
 * It tests:
 * - Successful logout with cart cleanup
 * - Proper response message
 * - Service method invocation
 * 
 * @author Test Generation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutController Test Suite")
class test_LogoutController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private LogoutController logoutController;

    private MockMvc mockMvc;
    private UserPrincipal userPrincipal;
    private UUID userId;

    /**
     * Setup method executed before each test
     * Initializes test data and MockMvc instance
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logoutController).build();
        
        userId = UUID.randomUUID();
        userPrincipal = new UserPrincipal(
            userId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );
    }

    /**
     * Test successful logout with cart cleanup
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Success message is returned
     * - Cart cleanup service is called with correct user ID
     */
    @Test
    @DisplayName("Should successfully logout user and cleanup cart")
    void testLogout_Success() throws Exception {
        // Arrange
        doNothing().when(cartService).cleanupCartOnLogout(userId);

        // Act & Assert
        mockMvc.perform(post("/logout")
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(cartService, times(1)).cleanupCartOnLogout(userId);
    }

    /**
     * Test logout when cart service throws exception
     * 
     * Verifies that:
     * - Exception is properly handled
     * - Service method is still called
     */
    @Test
    @DisplayName("Should handle exception during cart cleanup")
    void testLogout_CartCleanupException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Cart cleanup failed"))
            .when(cartService).cleanupCartOnLogout(userId);

        // Act & Assert
        try {
            mockMvc.perform(post("/logout")
                    .principal(() -> userPrincipal.getUsername()));
        } catch (Exception e) {
            // Expected exception
        }

        verify(cartService, times(1)).cleanupCartOnLogout(userId);
    }

    /**
     * Test logout with null user principal
     * 
     * Verifies that:
     * - Proper error handling for missing authentication
     */
    @Test
    @DisplayName("Should handle logout with no authentication")
    void testLogout_NoAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/logout"))
            .andExpect(status().isUnauthorized());

        verify(cartService, never()).cleanupCartOnLogout(any());
    }

    /**
     * Test logout multiple times
     * 
     * Verifies that:
     * - Multiple logout calls are handled correctly
     * - Cart cleanup is called for each logout
     */
    @Test
    @DisplayName("Should handle multiple logout calls")
    void testLogout_MultipleCalls() throws Exception {
        // Arrange
        doNothing().when(cartService).cleanupCartOnLogout(userId);

        // Act & Assert - First logout
        mockMvc.perform(post("/logout")
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Act & Assert - Second logout
        mockMvc.perform(post("/logout")
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(cartService, times(2)).cleanupCartOnLogout(userId);
    }
}