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
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for LogoutController
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
     * Verifies HTTP 200 status and success message
     */
    @Test
    void logoutShouldReturnOkStatusWithSuccessMessage() {
        // Given
        doNothing().when(cartService).clearCart(userId);

        // When
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout clears user cart
     * Verifies cart service is invoked with correct user ID
     */
    @Test
    void logoutShouldClearUserCart() {
        // Given
        doNothing().when(cartService).clearCart(userId);

        // When
        logoutController.logout(userId);

        // Then
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with null user ID
     * Verifies proper handling of null user ID
     */
    @Test
    void logoutShouldHandleNullUserId() {
        // Given
        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            logoutController.logout(null);
        });
    }

    /**
     * Test logout when cart service throws exception
     * Verifies exception propagation
     */
    @Test
    void logoutShouldPropagateCartServiceException() {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(cartService).clearCart(userId);

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            logoutController.logout(userId);
        });
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with non-existent user
     * Verifies handling when user has no cart
     */
    @Test
    void logoutShouldHandleNonExistentUserCart() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        doNothing().when(cartService).clearCart(nonExistentUserId);

        // When
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(nonExistentUserId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).clearCart(nonExistentUserId);
    }

    /**
     * Test multiple logout calls for same user
     * Verifies idempotency of logout operation
     */
    @Test
    void logoutShouldBeIdempotent() {
        // Given
        doNothing().when(cartService).clearCart(userId);

        // When
        ResponseEntity<ApiResponse<Void>> response1 = logoutController.logout(userId);
        ResponseEntity<ApiResponse<Void>> response2 = logoutController.logout(userId);

        // Then
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(cartService, times(2)).clearCart(userId);
    }

    /**
     * Test logout response structure
     * Verifies ApiResponse contains correct fields
     */
    @Test
    void logoutShouldReturnCorrectApiResponseStructure() {
        // Given
        doNothing().when(cartService).clearCart(userId);

        // When
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        // Then
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("successful"));
        assertNull(response.getBody().getData());
    }
}