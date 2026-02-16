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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for UserController
 * Tests user management operations including signup, login, profile retrieval and update
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
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
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
     * Verifies HTTP 201 status and user response
     */
    @Test
    void signupShouldReturnCreatedStatusWithUserResponse() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setEmail("test@example.com");

        when(userService.signup(any(SignupRequest.class))).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.signup(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with duplicate username
     * Verifies exception handling for duplicate user
     */
    @Test
    void signupShouldHandleDuplicateUsername() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setEmail("test@example.com");

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.signup(request);
        });
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with invalid email format
     * Verifies email validation
     */
    @Test
    void signupShouldValidateEmailFormat() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setEmail("invalid-email");

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(request);
        });
    }

    /**
     * Test signup with weak password
     * Verifies password strength validation
     */
    @Test
    void signupShouldValidatePasswordStrength() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setPassword("123");
        request.setFullName("Test User");
        request.setEmail("test@example.com");

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Password too weak"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(request);
        });
    }

    /**
     * Test successful user login
     * Verifies HTTP 200 status and user response
     */
    @Test
    void loginShouldReturnOkStatusWithUserResponse() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userService.login(any(LoginRequest.class))).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.login(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies authentication failure handling
     */
    @Test
    void loginShouldHandleInvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.login(request);
        });
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with non-existent user
     * Verifies user not found handling
     */
    @Test
    void loginShouldHandleNonExistentUser() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.login(request);
        });
    }

    /**
     * Test getting user profile successfully
     * Verifies HTTP 200 status and profile data
     */
    @Test
    void getProfileShouldReturnOkStatusWithUserProfile() {
        // Given
        when(userService.getProfile(userId)).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = userController.getProfile(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).getProfile(userId);
    }

    /**
     * Test getting profile for non-existent user
     * Verifies exception handling
     */
    @Test
    void getProfileShouldHandleNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getProfile(nonExistentId))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.getProfile(nonExistentId);
        });
    }

    /**
     * Test updating user profile successfully
     * Verifies HTTP 200 status and updated profile
     */
    @Test
    void updateProfileShouldReturnOkStatusWithUpdatedProfile() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        UserResponse updatedResponse = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .fullName("Updated Name")
            .email("updated@example.com")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
            .thenReturn(updatedResponse);

        // When
        ResponseEntity<UserResponse> response = userController.updateProfile(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().getFullName());
        assertEquals("updated@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with invalid email
     * Verifies email validation on update
     */
    @Test
    void updateProfileShouldValidateEmail() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("invalid-email");

        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(userId, request);
        });
    }

    /**
     * Test updating profile for non-existent user
     * Verifies exception handling
     */
    @Test
    void updateProfileShouldHandleNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        when(userService.updateProfile(eq(nonExistentId), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(nonExistentId, request);
        });
    }

    /**
     * Test signup with missing required fields
     * Verifies validation of required fields
     */
    @Test
    void signupShouldValidateRequiredFields() {
        // Given
        SignupRequest request = new SignupRequest();
        // Missing required fields

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Required fields missing"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(request);
        });
    }
}