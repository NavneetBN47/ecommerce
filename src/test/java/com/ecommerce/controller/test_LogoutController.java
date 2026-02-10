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
 * This test class validates the logout functionality of the e-commerce application,
 * ensuring proper cart clearing, response handling, and error scenarios.
 * 
 * @author WF-4B Test Generation Agent
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
     * Sets up test data before each test execution.
     * Initializes a valid test user ID for use across test cases.
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    /**
     * Tests successful logout with valid user ID.
     * 
     * Verifies that:
     * - CartService.clearCart is called with correct user ID
     * - Response status is 200 OK
     * - Response body contains success message
     * - Response data is null as expected
     */
    @Test
    @DisplayName("Should successfully logout user with valid user ID")
    void testLogout_WithValidUserId_ShouldReturnSuccessResponse() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Logout successful", response.getBody().getMessage(), "Success message should match");
        assertNull(response.getBody().getData(), "Response data should be null");
        
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Tests logout with null user ID.
     * 
     * Verifies that the controller handles null user ID gracefully,
     * though in production this would typically be caught by request validation.
     */
    @Test
    @DisplayName("Should handle logout with null user ID")
    void testLogout_WithNullUserId_ShouldCallClearCart() {
        // Arrange
        UUID nullUserId = null;
        doNothing().when(cartService).clearCart(nullUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(nullUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be 200 OK");
        
        verify(cartService, times(1)).clearCart(nullUserId);
    }

    /**
     * Tests logout when CartService throws RuntimeException.
     * 
     * Verifies that exceptions from the service layer are properly propagated,
     * allowing the global exception handler to process them.
     */
    @Test
    @DisplayName("Should propagate exception when CartService fails")
    void testLogout_WhenCartServiceThrowsException_ShouldPropagateException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Cart service error");
        doThrow(expectedException).when(cartService).clearCart(testUserId);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, 
            () -> logoutController.logout(testUserId),
            "Should throw RuntimeException when CartService fails");
        
        assertEquals("Cart service error", thrownException.getMessage(), "Exception message should match");
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Tests logout with different valid user IDs.
     * 
     * Verifies that the logout functionality works correctly with multiple
     * different user IDs, ensuring no state is shared between requests.
     */
    @Test
    @DisplayName("Should handle logout for multiple different user IDs")
    void testLogout_WithMultipleUserIds_ShouldHandleEachIndependently() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        
        doNothing().when(cartService).clearCart(any(UUID.class));

        // Act
        ResponseEntity<ApiResponse<Void>> response1 = logoutController.logout(userId1);
        ResponseEntity<ApiResponse<Void>> response2 = logoutController.logout(userId2);
        ResponseEntity<ApiResponse<Void>> response3 = logoutController.logout(userId3);

        // Assert
        assertAll("All logout responses should be successful",
            () -> assertEquals(HttpStatus.OK, response1.getStatusCode()),
            () -> assertEquals(HttpStatus.OK, response2.getStatusCode()),
            () -> assertEquals(HttpStatus.OK, response3.getStatusCode())
        );
        
        verify(cartService, times(1)).clearCart(userId1);
        verify(cartService, times(1)).clearCart(userId2);
        verify(cartService, times(1)).clearCart(userId3);
    }

    /**
     * Tests that logout response structure is correct.
     * 
     * Verifies the ApiResponse wrapper contains all expected fields
     * and follows the defined contract.
     */
    @Test
    @DisplayName("Should return correctly structured ApiResponse")
    void testLogout_ShouldReturnCorrectApiResponseStructure() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertAll("ApiResponse structure validation",
            () -> assertNotNull(apiResponse.getMessage(), "Message should not be null"),
            () -> assertTrue(apiResponse.getMessage().contains("successful"), "Message should indicate success"),
            () -> assertNull(apiResponse.getData(), "Data should be null for logout")
        );
        
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Tests logout with edge case UUID (all zeros).
     * 
     * Verifies that the controller handles edge case UUIDs correctly.
     */
    @Test
    @DisplayName("Should handle logout with edge case UUID")
    void testLogout_WithEdgeCaseUUID_ShouldSucceed() {
        // Arrange
        UUID edgeCaseUserId = new UUID(0L, 0L);
        doNothing().when(cartService).clearCart(edgeCaseUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(edgeCaseUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be 200 OK");
        
        verify(cartService, times(1)).clearCart(edgeCaseUserId);
    }

    /**
     * Tests that CartService is called exactly once per logout request.
     * 
     * Verifies proper interaction with the service layer and ensures
     * no duplicate calls are made.
     */
    @Test
    @DisplayName("Should call CartService.clearCart exactly once")
    void testLogout_ShouldCallClearCartExactlyOnce() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        logoutController.logout(testUserId);

        // Assert
        verify(cartService, times(1)).clearCart(testUserId);
        verifyNoMoreInteractions(cartService);
    }

    /**
     * Tests logout when CartService.clearCart completes successfully.
     * 
     * Verifies that successful cart clearing results in a proper success response
     * without any errors.
     */
    @Test
    @DisplayName("Should return success when cart is cleared successfully")
    void testLogout_WhenCartClearedSuccessfully_ShouldReturnSuccess() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Logout successful", response.getBody().getMessage(), "Success message should be correct");
        
        verify(cartService, times(1)).clearCart(testUserId);
    }
}
