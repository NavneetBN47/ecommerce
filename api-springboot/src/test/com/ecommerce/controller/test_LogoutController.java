package com.ecommerce.controller;

import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test class for LogoutController
 * 
 * This test class verifies the REST API endpoint for user logout,
 * including cart cleanup functionality.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_LogoutController {

    @Mock
    private CartService cartService;

    @Mock
    private UserPrincipal currentUser;

    @InjectMocks
    private LogoutController logoutController;

    private UUID userId;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);
    }

    /**
     * Test successful logout with cart cleanup
     * 
     * Verifies that logout endpoint properly cleans up the user's cart
     * and returns HTTP 200 OK with success message.
     */
    @Test
    void logout_WithValidUser_ShouldCleanupCartAndReturnSuccessMessage() {
        doNothing().when(cartService).cleanupCartOnLogout(userId);

        ResponseEntity<Map<String, String>> response = logoutController.logout(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message")).isEqualTo("Logged out successfully");
        verify(cartService, times(1)).cleanupCartOnLogout(userId);
    }

    /**
     * Test logout calls cart cleanup service
     * 
     * Verifies that the logout endpoint invokes the cart cleanup
     * service method exactly once.
     */
    @Test
    void logout_ShouldCallCartCleanupService() {
        doNothing().when(cartService).cleanupCartOnLogout(userId);

        logoutController.logout(currentUser);

        verify(cartService, times(1)).cleanupCartOnLogout(userId);
    }

    /**
     * Test logout with different user IDs
     * 
     * Verifies that logout works correctly for different users
     * and cleans up the correct user's cart.
     */
    @Test
    void logout_WithDifferentUsers_ShouldCleanupCorrectCart() {
        UUID anotherUserId = UUID.randomUUID();
        UserPrincipal anotherUser = mock(UserPrincipal.class);
        when(anotherUser.getId()).thenReturn(anotherUserId);

        doNothing().when(cartService).cleanupCartOnLogout(anotherUserId);

        ResponseEntity<Map<String, String>> response = logoutController.logout(anotherUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cartService, times(1)).cleanupCartOnLogout(anotherUserId);
        verify(cartService, never()).cleanupCartOnLogout(userId);
    }

    /**
     * Test logout response message format
     * 
     * Verifies that the logout response contains the correct
     * message format and structure.
     */
    @Test
    void logout_ShouldReturnCorrectMessageFormat() {
        doNothing().when(cartService).cleanupCartOnLogout(userId);

        ResponseEntity<Map<String, String>> response = logoutController.logout(currentUser);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).containsOnlyKeys("message");
        assertThat(response.getBody().get("message")).isNotEmpty();
    }
}