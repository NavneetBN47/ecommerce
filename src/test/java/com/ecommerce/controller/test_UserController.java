package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test class for UserController.
 * Tests all public endpoints with proper mocking of UserService and CartService dependencies.
 * Covers normal execution paths, edge cases, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class test_UserController {

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Test successful user registration.
     * Verifies that the controller returns user response with CREATED status.
     */
    @Test
    @DisplayName("Should register user successfully")
    void testRegister_Success() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setEmail("test@example.com");

        UserResponse expectedResponse = UserResponse.builder()
            .userId(1L)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.registerUser(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.register(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userService, times(1)).registerUser(request);
    }

    /**
     * Test user registration with minimum required fields.
     * Verifies that registration works with only required fields.
     */
    @Test
    @DisplayName("Should register user with minimum required fields")
    void testRegister_MinimumFields() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("minuser");
        request.setPassword("password123");
        request.setFullName("Min User");

        UserResponse expectedResponse = UserResponse.builder()
            .userId(2L)
            .username("minuser")
            .fullName("Min User")
            .email(null)
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.registerUser(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.register(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertNull(response.getBody().getEmail());
        verify(userService, times(1)).registerUser(request);
    }

    /**
     * Test user registration with duplicate username.
     * Verifies that service layer exceptions are properly propagated.
     */
    @Test
    @DisplayName("Should handle duplicate username registration")
    void testRegister_DuplicateUsername() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setFullName("Existing User");

        when(userService.registerUser(request))
            .thenThrow(new RuntimeException("Username already exists"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.register(request);
        });
        
        verify(userService, times(1)).registerUser(request);
    }

    /**
     * Test successful user login.
     * Verifies that the controller returns login response with OK status.
     */
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() {
        // Given
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        LoginResponse expectedResponse = LoginResponse.builder()
            .token("jwt-token-123")
            .userId(1L)
            .username("testuser")
            .build();

        when(userService.login(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<LoginResponse> response = userController.login(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals("jwt-token-123", response.getBody().getToken());
        assertEquals(1L, response.getBody().getUserId());
        verify(userService, times(1)).login(request);
    }

    /**
     * Test login with invalid credentials.
     * Verifies that invalid credential exceptions are properly propagated.
     */
    @Test
    @DisplayName("Should handle invalid login credentials")
    void testLogin_InvalidCredentials() {
        // Given
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userService.login(request))
            .thenThrow(new RuntimeException("Invalid username or password"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.login(request);
        });
        
        verify(userService, times(1)).login(request);
    }

    /**
     * Test login with non-existent user.
     * Verifies that non-existent user scenarios are handled appropriately.
     */
    @Test
    @DisplayName("Should handle login for non-existent user")
    void testLogin_NonExistentUser() {
        // Given
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userService.login(request))
            .thenThrow(new RuntimeException("User not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.login(request);
        });
        
        verify(userService, times(1)).login(request);
    }

    /**
     * Test successful user logout.
     * Verifies that the controller clears cart and returns NO_CONTENT status.
     */
    @Test
    @DisplayName("Should logout user successfully")
    void testLogout_Success() {
        // Given
        Long userId = 1L;

        doNothing().when(cartService).clearCart(userId);

        // When
        ResponseEntity<Void> response = userController.logout(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with null user ID.
     * Verifies that null user ID scenarios are handled appropriately.
     */
    @Test
    @DisplayName("Should handle logout with null user ID")
    void testLogout_NullUserId() {
        // Given
        Long userId = null;

        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(userId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userController.logout(userId);
        });
        
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test successful user profile retrieval.
     * Verifies that the controller returns user profile with OK status.
     */
    @Test
    @DisplayName("Should get user profile successfully")
    void testGetProfile_Success() {
        // Given
        Long userId = 1L;
        UserResponse expectedResponse = UserResponse.builder()
            .userId(userId)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.getUserProfile(userId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.getProfile(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        verify(userService, times(1)).getUserProfile(userId);
    }

    /**
     * Test profile retrieval for non-existent user.
     * Verifies that service layer exceptions are properly propagated.
     */
    @Test
    @DisplayName("Should handle profile retrieval for non-existent user")
    void testGetProfile_UserNotFound() {
        // Given
        Long userId = 999L;

        when(userService.getUserProfile(userId))
            .thenThrow(new RuntimeException("User not found: " + userId));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.getProfile(userId);
        });
        
        verify(userService, times(1)).getUserProfile(userId);
    }

    /**
     * Test successful user profile update.
     * Verifies that the controller returns updated profile with OK status.
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_Success() {
        // Given
        Long userId = 1L;
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Updated User");
        request.setEmail("updated@example.com");

        UserResponse expectedResponse = UserResponse.builder()
            .userId(userId)
            .username("testuser")
            .fullName("Updated User")
            .email("updated@example.com")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.updateUserProfile(userId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.updateProfile(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals("Updated User", response.getBody().getFullName());
        assertEquals("updated@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateUserProfile(userId, request);
    }

    /**
     * Test profile update with partial data.
     * Verifies that partial updates are handled correctly.
     */
    @Test
    @DisplayName("Should update user profile with partial data")
    void testUpdateProfile_PartialUpdate() {
        // Given
        Long userId = 1L;
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Partially Updated User");
        // email is null - should not be updated

        UserResponse expectedResponse = UserResponse.builder()
            .userId(userId)
            .username("testuser")
            .fullName("Partially Updated User")
            .email("original@example.com") // original email preserved
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.updateUserProfile(userId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.updateProfile(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals("Partially Updated User", response.getBody().getFullName());
        assertEquals("original@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateUserProfile(userId, request);
    }

    /**
     * Test profile update for non-existent user.
     * Verifies that service layer exceptions are properly propagated.
     */
    @Test
    @DisplayName("Should handle profile update for non-existent user")
    void testUpdateProfile_UserNotFound() {
        // Given
        Long userId = 999L;
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Updated User");

        when(userService.updateUserProfile(userId, request))
            .thenThrow(new RuntimeException("User not found: " + userId));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, request);
        });
        
        verify(userService, times(1)).updateUserProfile(userId, request);
    }
}