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
 * JUnit 5 test class for UserController.
 * Tests user management operations including signup, login, profile retrieval, and profile updates.
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
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");

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
     * Test user signup successfully.
     * Verifies that new user can be registered and returns HTTP 201 CREATED status.
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
     * Test signup with null request.
     * Verifies proper exception handling.
     */
    @Test
    void testSignup_NullRequest() {
        assertThrows(Exception.class, () -> {
            userController.signup(null);
        });
    }

    /**
     * Test signup with duplicate username.
     * Verifies that duplicate username is rejected.
     */
    @Test
    void testSignup_DuplicateUsername() {
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        assertThrows(RuntimeException.class, () -> {
            userController.signup(signupRequest);
        });
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with invalid email format.
     * Verifies email validation.
     */
    @Test
    void testSignup_InvalidEmail() {
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("Password123!");

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(invalidRequest);
        });
    }

    /**
     * Test user login successfully.
     * Verifies that valid credentials return user details and HTTP 200 OK status.
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
     * Verifies that invalid credentials are rejected.
     */
    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("wronguser");
        invalidRequest.setPassword("wrongpass");

        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> {
            userController.login(invalidRequest);
        });
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with null request.
     * Verifies proper null handling.
     */
    @Test
    void testLogin_NullRequest() {
        assertThrows(Exception.class, () -> {
            userController.login(null);
        });
    }

    /**
     * Test getting user profile successfully.
     * Verifies that user profile is retrieved with HTTP 200 OK status.
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
     * Test getting profile for non-existent user.
     * Verifies proper error handling.
     */
    @Test
    void testGetProfile_UserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(userService.getProfile(eq(nonExistentUserId)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.getProfile(nonExistentUserId);
        });
    }

    /**
     * Test getting profile with null user ID.
     * Verifies proper null handling.
     */
    @Test
    void testGetProfile_NullUserId() {
        when(userService.getProfile(null))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.getProfile(null);
        });
    }

    /**
     * Test updating user profile successfully.
     * Verifies that profile is updated and returns HTTP 200 OK status.
     */
    @Test
    void testUpdateProfile_Success() {
        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setId(userId);
        updatedResponse.setUsername("testuser");
        updatedResponse.setEmail("updated@example.com");
        updatedResponse.setFullName("Updated User");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenReturn(updatedResponse);

        ResponseEntity<UserResponse> response = userController.updateProfile(userId, updateProfileRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated User", response.getBody().getFullName());
        assertEquals("updated@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with null request.
     * Verifies proper exception handling.
     */
    @Test
    void testUpdateProfile_NullRequest() {
        assertThrows(Exception.class, () -> {
            userController.updateProfile(userId, null);
        });
    }

    /**
     * Test updating profile with invalid email.
     * Verifies email validation during update.
     */
    @Test
    void testUpdateProfile_InvalidEmail() {
        UpdateProfileRequest invalidRequest = new UpdateProfileRequest();
        invalidRequest.setEmail("invalid-email");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(userId, invalidRequest);
        });
    }

    /**
     * Test updating profile for non-existent user.
     * Verifies proper error handling.
     */
    @Test
    void testUpdateProfile_UserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(userService.updateProfile(eq(nonExistentUserId), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(nonExistentUserId, updateProfileRequest);
        });
    }

    /**
     * Test updating profile with duplicate email.
     * Verifies that duplicate email is rejected.
     */
    @Test
    void testUpdateProfile_DuplicateEmail() {
        UpdateProfileRequest duplicateEmailRequest = new UpdateProfileRequest();
        duplicateEmailRequest.setEmail("existing@example.com");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("Email already in use"));

        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, duplicateEmailRequest);
        });
    }
}