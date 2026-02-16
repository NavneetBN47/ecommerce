package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for LogoutController
 * 
 * Tests logout functionality and cart cleanup
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_LogoutController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private LogoutController logoutController;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    /**
     * Test successful logout
     * 
     * Verifies:
     * - Logout endpoint returns OK status
     * - Cart is cleared for the user
     * - Success message is returned
     */
    @Test
    void testLogout_Success() {
        // Given
        doNothing().when(cartService).clearCart(any(UUID.class));

        // When
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData(), "Data should be null for logout");
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with null user ID
     * 
     * Verifies:
     * - Appropriate exception handling for null user ID
     */
    @Test
    void testLogout_NullUserId() {
        // When/Then
        assertThrows(Exception.class, () -> {
            logoutController.logout(null);
        });
    }

    /**
     * Test logout when cart service throws exception
     * 
     * Verifies:
     * - Exception from cart service is propagated
     */
    @Test
    void testLogout_CartServiceException() {
        // Given
        doThrow(new RuntimeException("Cart service error"))
            .when(cartService).clearCart(any(UUID.class));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            logoutController.logout(userId);
        });
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with valid user ID format
     * 
     * Verifies:
     * - UUID format is correctly handled
     * - Cart service receives correct user ID
     */
    @Test
    void testLogout_ValidUUIDFormat() {
        // Given
        UUID specificUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doNothing().when(cartService).clearCart(specificUserId);

        // When
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(specificUserId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        verify(cartService, times(1)).clearCart(specificUserId);
    }
}
