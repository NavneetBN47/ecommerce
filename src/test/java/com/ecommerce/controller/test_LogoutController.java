package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
 * JUnit test class for LogoutController.
 * 
 * This test class provides comprehensive test coverage for the LogoutController,
 * including normal execution paths, edge cases, and exception scenarios.
 * 
 * @author JUnit Test Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutController Test Suite")
class test_LogoutController {

    @Mock
    private CartService cartService;

    @InjectMocks
    private LogoutController logoutController;

    private UUID testUserId;

    /**
     * Setup method executed before each test.
     * Initializes test data and mock objects.
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    /**
     * Test successful logout with valid user ID.
     * 
     * Verifies that:
     * - The logout endpoint returns HTTP 200 OK
     * - The response contains a success message
     * - The cartService.clearCart() is called exactly once with the correct user ID
     * - The response body contains the expected ApiResponse structure
     */
    @Test
    @DisplayName("Should successfully logout user with valid user ID")
    void testLogout_WithValidUserId_ReturnsSuccessResponse() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().isSuccess(), "Response should indicate success");
        assertEquals("Logout successful", response.getBody().getMessage(), "Success message should match");
        assertNull(response.getBody().getData(), "Data should be null for logout response");
        
        verify(cartService, times(1)).clearCart(testUserId);
        verifyNoMoreInteractions(cartService);
    }

    /**
     * Test logout with different valid UUID.
     * 
     * Verifies that the logout process works correctly with various valid UUIDs.
     */
    @Test
    @DisplayName("Should successfully logout user with different valid UUID")
    void testLogout_WithDifferentValidUserId_ReturnsSuccessResponse() {
        // Arrange
        UUID anotherUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doNothing().when(cartService).clearCart(anotherUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(anotherUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logout successful", response.getBody().getMessage());
        
        verify(cartService, times(1)).clearCart(anotherUserId);
    }

    /**
     * Test logout when cartService.clearCart() throws RuntimeException.
     * 
     * Verifies that exceptions from the service layer are properly propagated.
     */
    @Test
    @DisplayName("Should propagate exception when cart service fails")
    void testLogout_WhenCartServiceThrowsException_PropagatesException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Cart service error");
        doThrow(expectedException).when(cartService).clearCart(testUserId);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            logoutController.logout(testUserId);
        }, "Should throw RuntimeException when cart service fails");

        assertEquals("Cart service error", thrownException.getMessage());
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout with minimum valid UUID (all zeros).
     * 
     * Verifies edge case handling with UUID containing all zeros.
     */
    @Test
    @DisplayName("Should handle logout with minimum UUID value")
    void testLogout_WithMinimumUUID_ReturnsSuccessResponse() {
        // Arrange
        UUID minUserId = new UUID(0L, 0L);
        doNothing().when(cartService).clearCart(minUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(minUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(cartService, times(1)).clearCart(minUserId);
    }

    /**
     * Test logout with maximum valid UUID (all ones).
     * 
     * Verifies edge case handling with UUID containing maximum values.
     */
    @Test
    @DisplayName("Should handle logout with maximum UUID value")
    void testLogout_WithMaximumUUID_ReturnsSuccessResponse() {
        // Arrange
        UUID maxUserId = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        doNothing().when(cartService).clearCart(maxUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(maxUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(cartService, times(1)).clearCart(maxUserId);
    }

    /**
     * Test that cart service is called with exact user ID parameter.
     * 
     * Verifies parameter passing integrity between controller and service layer.
     */
    @Test
    @DisplayName("Should pass correct user ID to cart service")
    void testLogout_VerifiesCorrectUserIdPassedToService() {
        // Arrange
        UUID specificUserId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        doNothing().when(cartService).clearCart(specificUserId);

        // Act
        logoutController.logout(specificUserId);

        // Assert
        verify(cartService).clearCart(eq(specificUserId));
    }

    /**
     * Test logout response structure and content.
     * 
     * Verifies that the ApiResponse object is properly constructed with all required fields.
     */
    @Test
    @DisplayName("Should return properly structured ApiResponse")
    void testLogout_ReturnsProperlyStructuredApiResponse() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        ApiResponse<Void> apiResponse = response.getBody();
        
        assertTrue(apiResponse.isSuccess(), "ApiResponse should indicate success");
        assertNotNull(apiResponse.getMessage(), "Message should not be null");
        assertFalse(apiResponse.getMessage().isEmpty(), "Message should not be empty");
        assertEquals("Logout successful", apiResponse.getMessage());
        assertNull(apiResponse.getData(), "Data should be null for void response");
    }

    /**
     * Test multiple consecutive logout calls for same user.
     * 
     * Verifies that multiple logout requests are handled independently.
     */
    @Test
    @DisplayName("Should handle multiple consecutive logout calls")
    void testLogout_MultipleConsecutiveCalls_EachCallSucceeds() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response1 = logoutController.logout(testUserId);
        ResponseEntity<ApiResponse<Void>> response2 = logoutController.logout(testUserId);
        ResponseEntity<ApiResponse<Void>> response3 = logoutController.logout(testUserId);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        
        verify(cartService, times(3)).clearCart(testUserId);
    }

    /**
     * Test logout when cart service throws IllegalArgumentException.
     * 
     * Verifies proper exception handling for invalid arguments.
     */
    @Test
    @DisplayName("Should propagate IllegalArgumentException from cart service")
    void testLogout_WhenCartServiceThrowsIllegalArgumentException_PropagatesException() {
        // Arrange
        IllegalArgumentException expectedException = new IllegalArgumentException("Invalid user ID");
        doThrow(expectedException).when(cartService).clearCart(testUserId);

        // Act & Assert
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
            logoutController.logout(testUserId);
        });

        assertEquals("Invalid user ID", thrownException.getMessage());
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout when cart service throws NullPointerException.
     * 
     * Verifies proper exception handling for null pointer scenarios.
     */
    @Test
    @DisplayName("Should propagate NullPointerException from cart service")
    void testLogout_WhenCartServiceThrowsNullPointerException_PropagatesException() {
        // Arrange
        NullPointerException expectedException = new NullPointerException("Null cart reference");
        doThrow(expectedException).when(cartService).clearCart(testUserId);

        // Act & Assert
        NullPointerException thrownException = assertThrows(NullPointerException.class, () -> {
            logoutController.logout(testUserId);
        });

        assertEquals("Null cart reference", thrownException.getMessage());
        verify(cartService, times(1)).clearCart(testUserId);
    }
}