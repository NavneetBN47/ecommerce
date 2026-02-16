package com.ecommerce.controller;

import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for LogoutController
 * 
 * Tests logout functionality including:
 * - Successful logout with cart cleanup
 * - Unauthorized access handling
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@WebMvcTest(LogoutController.class)
@DisplayName("LogoutController Tests")
class test_LogoutController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    private UserPrincipal testUser;
    private UUID testUserId;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new UserPrincipal(
            testUserId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );
    }

    /**
     * Test successful logout
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Success message in response
     * - Cart cleanup service invocation
     */
    @Test
    @WithMockUser
    @DisplayName("Should logout successfully and cleanup cart")
    void testLogout_Success() throws Exception {
        doNothing().when(cartService).cleanupCartOnLogout(any(UUID.class));

        mockMvc.perform(post("/logout")
                .with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(cartService).cleanupCartOnLogout(testUserId);
    }

    /**
     * Test logout without authentication
     * 
     * Validates:
     * - HTTP 401 Unauthorized for unauthenticated requests
     */
    @Test
    @DisplayName("Should return 401 for unauthenticated logout")
    void testLogout_Unauthorized() throws Exception {
        mockMvc.perform(post("/logout"))
            .andExpect(status().isUnauthorized());
    }
}