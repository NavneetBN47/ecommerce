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
 * JUnit 5 test class for LogoutController.
 * Tests logout operations and cart cleanup functionality.
 */
@ExtendWith(MockitoExtension.class)
class test_LogoutController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private LogoutController logoutController;

    private UUID userId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    /**
     * Test successful logout operation.
     * Verifies that user logout clears the cart and returns success response.
     */
    @Test
    void testLogout_Success() {
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test logout with null user ID.
     * Verifies proper handling of null user ID.
     */
    @Test
    void testLogout_NullUserId() {
        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(null);

        assertThrows(IllegalArgumentException.class, () -> {
            logoutController.logout(null);
        });
        verify(cartService, times(1)).clearCart(null);
    }

    /**
     * Test logout when cart service throws exception.
     * Verifies proper error handling during cart clearing operation.
     */
    @Test
    void testLogout_ServiceException() {
        doThrow(new RuntimeException("Failed to clear cart"))
            .when(cartService).clearCart(any(UUID.class));

        assertThrows(RuntimeException.class, () -> {
            logoutController.logout(userId);
        });
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test logout with invalid UUID format.
     * Verifies handling of malformed user ID.
     */
    @Test
    void testLogout_InvalidUUID() {
        UUID invalidUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        doThrow(new IllegalArgumentException("Invalid user ID"))
            .when(cartService).clearCart(eq(invalidUserId));

        assertThrows(IllegalArgumentException.class, () -> {
            logoutController.logout(invalidUserId);
        });
        verify(cartService, times(1)).clearCart(eq(invalidUserId));
    }

    /**
     * Test logout for non-existent user.
     * Verifies handling when user does not exist.
     */
    @Test
    void testLogout_UserNotFound() {
        doThrow(new RuntimeException("User not found"))
            .when(cartService).clearCart(any(UUID.class));

        assertThrows(RuntimeException.class, () -> {
            logoutController.logout(userId);
        });
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test logout when cart is already empty.
     * Verifies that logout succeeds even when cart is empty.
     */
    @Test
    void testLogout_EmptyCart() {
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test multiple logout calls for same user.
     * Verifies idempotency of logout operation.
     */
    @Test
    void testLogout_MultipleCallsSameUser() {
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<ApiResponse<Void>> response1 = logoutController.logout(userId);
        ResponseEntity<ApiResponse<Void>> response2 = logoutController.logout(userId);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(cartService, times(2)).clearCart(eq(userId));
    }

    /**
     * Test logout response structure.
     * Verifies that response contains all required fields.
     */
    @Test
    void testLogout_ResponseStructure() {
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().isEmpty());
        assertNull(response.getBody().getData());
    }
}