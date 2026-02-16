package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for UserController
 * 
 * Tests user operations including signup, login, profile retrieval and update
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_UserController {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");
        signupRequest.setEmail("test@example.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setFullName("Updated Name");
        updateProfileRequest.setEmail("updated@example.com");

        userResponse = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * Test successful user signup
     * 
     * Verifies:
     * - User is registered successfully
     * - Returns CREATED status
     * - UserService.signup is called with correct request
     */
    @Test
    void testSignup_Success() {
        // Given
        when(userService.signup(any(SignupRequest.class))).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.signup(signupRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be CREATED");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).signup(signupRequest);
    }

    /**
     * Test signup with duplicate username
     * 
     * Verifies:
     * - Appropriate exception is thrown for duplicate username
     */
    @Test
    void testSignup_DuplicateUsername() {
        // Given
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.signup(signupRequest);
        });
    }

    /**
     * Test signup with invalid email format
     * 
     * Verifies:
     * - Validation rejects invalid email format
     */
    @Test
    void testSignup_InvalidEmail() {
        // Given
        signupRequest.setEmail("invalid-email");
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Invalid email format"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.signup(signupRequest);
        });
    }

    /**
     * Test successful user login
     * 
     * Verifies:
     * - User is logged in successfully
     * - Returns OK status
     * - UserService.login is called with correct credentials
     */
    @Test
    void testLogin_Success() {
        // Given
        when(userService.login(any(LoginRequest.class))).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.login(loginRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).login(loginRequest);
    }

    /**
     * Test login with invalid credentials
     * 
     * Verifies:
     * - Appropriate exception is thrown for invalid credentials
     */
    @Test
    void testLogin_InvalidCredentials() {
        // Given
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.login(loginRequest);
        });
    }

    /**
     * Test login with non-existent user
     * 
     * Verifies:
     * - Appropriate exception is thrown for non-existent user
     */
    @Test
    void testLogin_UserNotFound() {
        // Given
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.login(loginRequest);
        });
    }

    /**
     * Test getting user profile successfully
     * 
     * Verifies:
     * - User profile is retrieved
     * - Returns OK status
     * - UserService.getProfile is called with correct user ID
     */
    @Test
    void testGetProfile_Success() {
        // Given
        when(userService.getProfile(any(UUID.class))).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.getProfile(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).getProfile(userId);
    }

    /**
     * Test getting profile for non-existent user
     * 
     * Verifies:
     * - Appropriate exception is thrown for non-existent user
     */
    @Test
    void testGetProfile_UserNotFound() {
        // Given
        when(userService.getProfile(any(UUID.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.getProfile(userId);
        });
    }

    /**
     * Test updating user profile successfully
     * 
     * Verifies:
     * - User profile is updated
     * - Returns OK status
     * - UserService.updateProfile is called with correct parameters
     */
    @Test
    void testUpdateProfile_Success() {
        // Given
        UserResponse updatedResponse = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .fullName("Updated Name")
            .email("updated@example.com")
            .createdAt(LocalDateTime.now())
            .build();
        
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenReturn(updatedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.updateProfile(userId, updateProfileRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Updated Name", response.getBody().getFullName());
        assertEquals("updated@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateProfile(userId, updateProfileRequest);
    }

    /**
     * Test updating profile with invalid data
     * 
     * Verifies:
     * - Validation rejects invalid profile data
     */
    @Test
    void testUpdateProfile_InvalidData() {
        // Given
        updateProfileRequest.setEmail("invalid-email");
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("Invalid email format"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
    }

    /**
     * Test updating profile for non-existent user
     * 
     * Verifies:
     * - Appropriate exception is thrown for non-existent user
     */
    @Test
    void testUpdateProfile_UserNotFound() {
        // Given
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
    }
}
