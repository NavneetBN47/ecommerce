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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for UserController.
 * Tests user management operations including signup, login, profile retrieval, and profile updates.
 */
@ExtendWith(MockitoExtension.class)
class test_UserController {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setFullName("Updated User");
        updateProfileRequest.setEmail("updated@example.com");

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("Test User");
    }

    /**
     * Test successfully signing up a new user.
     * Verifies that a new user can be registered and returns CREATED status.
     */
    @Test
    void testSignup_Success() {
        when(userService.signup(any(SignupRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.signup(signupRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with duplicate username.
     * Verifies proper exception handling for duplicate username.
     */
    @Test
    void testSignup_DuplicateUsername() {
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        assertThrows(RuntimeException.class, () -> userController.signup(signupRequest));
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with invalid email format.
     * Verifies validation of email format.
     */
    @Test
    void testSignup_InvalidEmail() {
        signupRequest.setEmail("invalid-email");
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, () -> userController.signup(signupRequest));
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with null request.
     * Verifies proper handling of null input.
     */
    @Test
    void testSignup_NullRequest() {
        when(userService.signup(null))
            .thenThrow(new IllegalArgumentException("Signup request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> userController.signup(null));
        verify(userService, times(1)).signup(null);
    }

    /**
     * Test successfully logging in a user.
     * Verifies that a user can login with valid credentials.
     */
    @Test
    void testLogin_Success() {
        when(userService.login(any(LoginRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with invalid credentials.
     * Verifies proper exception handling for authentication failures.
     */
    @Test
    void testLogin_InvalidCredentials() {
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid username or password"));

        assertThrows(RuntimeException.class, () -> userController.login(loginRequest));
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with null request.
     * Verifies proper handling of null input.
     */
    @Test
    void testLogin_NullRequest() {
        when(userService.login(null))
            .thenThrow(new IllegalArgumentException("Login request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> userController.login(null));
        verify(userService, times(1)).login(null);
    }

    /**
     * Test login with empty username.
     * Verifies validation of required fields.
     */
    @Test
    void testLogin_EmptyUsername() {
        loginRequest.setUsername("");
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Username cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> userController.login(loginRequest));
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test successfully retrieving user profile.
     * Verifies that user profile can be fetched.
     */
    @Test
    void testGetProfile_Success() {
        when(userService.getProfile(any(UUID.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getProfile(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userService, times(1)).getProfile(eq(userId));
    }

    /**
     * Test retrieving profile for non-existent user.
     * Verifies proper exception handling for missing user.
     */
    @Test
    void testGetProfile_UserNotFound() {
        when(userService.getProfile(any(UUID.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> userController.getProfile(userId));
        verify(userService, times(1)).getProfile(eq(userId));
    }

    /**
     * Test retrieving profile with null user ID.
     * Verifies proper handling of null user ID.
     */
    @Test
    void testGetProfile_NullUserId() {
        when(userService.getProfile(null))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> userController.getProfile(null));
        verify(userService, times(1)).getProfile(null);
    }

    /**
     * Test successfully updating user profile.
     * Verifies that user profile can be updated.
     */
    @Test
    void testUpdateProfile_Success() {
        userResponse.setFullName("Updated User");
        userResponse.setEmail("updated@example.com");
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.updateProfile(userId, updateProfileRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated User", response.getBody().getFullName());
        assertEquals("updated@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with invalid email.
     * Verifies validation of email format during update.
     */
    @Test
    void testUpdateProfile_InvalidEmail() {
        updateProfileRequest.setEmail("invalid-email");
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, 
            () -> userController.updateProfile(userId, updateProfileRequest));
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with null request.
     * Verifies proper handling of null input.
     */
    @Test
    void testUpdateProfile_NullRequest() {
        when(userService.updateProfile(any(UUID.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Update request cannot be null"));

        assertThrows(IllegalArgumentException.class, 
            () -> userController.updateProfile(userId, null));
        verify(userService, times(1)).updateProfile(eq(userId), eq(null));
    }

    /**
     * Test updating profile for non-existent user.
     * Verifies proper exception handling for missing user.
     */
    @Test
    void testUpdateProfile_UserNotFound() {
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, 
            () -> userController.updateProfile(userId, updateProfileRequest));
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }
}