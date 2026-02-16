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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test class for LogoutController
 * Tests logout operations and cart clearing functionality
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
     * Verifies that user can logout successfully and cart is cleared
     */
    @Test
    void testLogout_Success() {
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test logout with null user ID
     * Verifies proper handling of null user ID
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
     * Test logout when cart service throws exception
     * Verifies proper error handling when cart clearing fails
     */
    @Test
    void testLogout_ServiceException() {
        doThrow(new RuntimeException("Cart service error"))
            .when(cartService).clearCart(any(UUID.class));

        assertThrows(RuntimeException.class, () -> {
            logoutController.logout(userId);
        });
        verify(cartService, times(1)).clearCart(eq(userId));
    }

    /**
     * Test logout with invalid UUID format
     * Verifies that valid UUID is properly processed
     */
    @Test
    void testLogout_ValidUUID() {
        UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doNothing().when(cartService).clearCart(any(UUID.class));

        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(validUserId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).clearCart(eq(validUserId));
    }

    /**
     * Test multiple logout calls for same user
     * Verifies idempotency of logout operation
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
}