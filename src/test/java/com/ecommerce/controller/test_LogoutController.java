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
 * JUnit 5 test class for LogoutController.
 * 
 * This test class provides comprehensive coverage for the logout functionality
 * including normal execution paths, edge cases, and validation scenarios.
 * Uses Mockito for mocking the CartService dependency.
 * 
 * @author QA Automation Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutController Tests")
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
     * - The response contains success message
     * - CartService.clearCart is called exactly once with correct userId
     * - Response body is properly structured
     */
    @Test
    @DisplayName("Should successfully logout user with valid userId")
    void testLogout_WithValidUserId_ShouldReturnSuccess() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Logout successful", response.getBody().getMessage(), "Success message should match");
        assertNull(response.getBody().getData(), "Data should be null for logout response");
        
        verify(cartService, times(1)).clearCart(testUserId);
        verifyNoMoreInteractions(cartService);
    }

    /**
     * Test logout with different valid UUID formats.
     * 
     * Verifies that the controller handles various valid UUID formats correctly.
     */
    @Test
    @DisplayName("Should handle logout with different valid UUID formats")
    void testLogout_WithDifferentValidUUIDs_ShouldSucceed() {
        // Arrange
        UUID uuid1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000000");
        UUID uuid3 = UUID.randomUUID();

        doNothing().when(cartService).clearCart(any(UUID.class));

        // Act & Assert for UUID 1
        ResponseEntity<ApiResponse<Void>> response1 = logoutController.logout(uuid1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        verify(cartService).clearCart(uuid1);

        // Act & Assert for UUID 2
        ResponseEntity<ApiResponse<Void>> response2 = logoutController.logout(uuid2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(cartService).clearCart(uuid2);

        // Act & Assert for UUID 3
        ResponseEntity<ApiResponse<Void>> response3 = logoutController.logout(uuid3);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        verify(cartService).clearCart(uuid3);

        verify(cartService, times(3)).clearCart(any(UUID.class));
    }

    /**
     * Test logout when CartService throws RuntimeException.
     * 
     * Verifies that exceptions from CartService are properly propagated
     * and not swallowed by the controller.
     */
    @Test
    @DisplayName("Should propagate exception when CartService fails")
    void testLogout_WhenCartServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String errorMessage = "Database connection failed";
        doThrow(new RuntimeException(errorMessage))
            .when(cartService).clearCart(testUserId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            logoutController.logout(testUserId);
        }, "Should throw RuntimeException when CartService fails");

        assertEquals(errorMessage, exception.getMessage(), "Exception message should match");
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout when CartService throws IllegalArgumentException.
     * 
     * Verifies proper handling of validation exceptions from the service layer.
     */
    @Test
    @DisplayName("Should propagate IllegalArgumentException from CartService")
    void testLogout_WhenCartServiceThrowsIllegalArgumentException_ShouldPropagate() {
        // Arrange
        String errorMessage = "Invalid user ID";
        doThrow(new IllegalArgumentException(errorMessage))
            .when(cartService).clearCart(testUserId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            logoutController.logout(testUserId);
        }, "Should throw IllegalArgumentException when validation fails");

        assertEquals(errorMessage, exception.getMessage());
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test logout response structure and content.
     * 
     * Verifies that the ApiResponse wrapper is correctly structured
     * with appropriate success indicators.
     */
    @Test
    @DisplayName("Should return properly structured ApiResponse")
    void testLogout_ResponseStructure_ShouldBeValid() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse.getMessage(), "Message should not be null");
        assertEquals("Logout successful", apiResponse.getMessage());
        assertNull(apiResponse.getData(), "Data should be null for void response");
        
        verify(cartService, times(1)).clearCart(testUserId);
    }

    /**
     * Test multiple consecutive logout calls for the same user.
     * 
     * Verifies that multiple logout requests are handled correctly
     * and CartService is called for each request.
     */
    @Test
    @DisplayName("Should handle multiple consecutive logout calls")
    void testLogout_MultipleConsecutiveCalls_ShouldSucceed() {
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
     * Test logout with minimum edge case UUID (all zeros).
     * 
     * Verifies handling of edge case UUID values.
     */
    @Test
    @DisplayName("Should handle logout with minimum UUID value")
    void testLogout_WithMinimumUUID_ShouldSucceed() {
        // Arrange
        UUID minUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        doNothing().when(cartService).clearCart(minUuid);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(minUuid);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        verify(cartService, times(1)).clearCart(minUuid);
    }

    /**
     * Test logout with maximum edge case UUID (all Fs).
     * 
     * Verifies handling of edge case UUID values.
     */
    @Test
    @DisplayName("Should handle logout with maximum UUID value")
    void testLogout_WithMaximumUUID_ShouldSucceed() {
        // Arrange
        UUID maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        doNothing().when(cartService).clearCart(maxUuid);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(maxUuid);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        verify(cartService, times(1)).clearCart(maxUuid);
    }

    /**
     * Test that CartService.clearCart is invoked before response is returned.
     * 
     * Verifies the order of operations in the logout flow.
     */
    @Test
    @DisplayName("Should call CartService.clearCart before returning response")
    void testLogout_ServiceCallOrder_ShouldBeCorrect() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        verify(cartService, times(1)).clearCart(testUserId);
        assertNotNull(response, "Response should be returned after service call");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test logout response HTTP status code.
     * 
     * Verifies that successful logout always returns 200 OK.
     */
    @Test
    @DisplayName("Should return HTTP 200 OK status for successful logout")
    void testLogout_HttpStatus_ShouldBe200OK() {
        // Arrange
        doNothing().when(cartService).clearCart(testUserId);

        // Act
        ResponseEntity<ApiResponse<Void>> response = logoutController.logout(testUserId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getStatusCodeValue());
        verify(cartService, times(1)).clearCart(testUserId);
    }
}
