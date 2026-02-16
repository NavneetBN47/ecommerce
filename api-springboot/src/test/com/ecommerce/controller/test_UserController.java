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

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private UserResponse userResponse;
    private UUID userId;

    /**
     * Set up test data before each test method execution.
     */
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
        updateProfileRequest.setFullName("Updated Name");
        updateProfileRequest.setEmail("updated@example.com");

        userResponse = new UserResponse();
        userResponse.setUserId(userId);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("Test User");
    }

    /**
     * Test successful user signup.
     * Verifies that new user is registered and returns HTTP 201 status.
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
     * Verifies proper handling of null signup request.
     */
    @Test
    void testSignup_NullRequest() {
        when(userService.signup(null))
            .thenThrow(new IllegalArgumentException("Signup request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(null);
        });
    }

    /**
     * Test signup with duplicate username.
     * Verifies handling of duplicate username.
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
     * Test signup with duplicate email.
     * Verifies handling of duplicate email address.
     */
    @Test
    void testSignup_DuplicateEmail() {
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        assertThrows(RuntimeException.class, () -> {
            userController.signup(signupRequest);
        });
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with invalid email format.
     * Verifies validation of email format.
     */
    @Test
    void testSignup_InvalidEmail() {
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(invalidRequest);
        });
    }

    /**
     * Test signup with weak password.
     * Verifies password strength validation.
     */
    @Test
    void testSignup_WeakPassword() {
        SignupRequest weakPasswordRequest = new SignupRequest();
        weakPasswordRequest.setUsername("testuser");
        weakPasswordRequest.setEmail("test@example.com");
        weakPasswordRequest.setPassword("123");

        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Password too weak"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(weakPasswordRequest);
        });
    }

    /**
     * Test successful user login.
     * Verifies that valid credentials result in successful login.
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
     * Test login with null request.
     * Verifies proper handling of null login request.
     */
    @Test
    void testLogin_NullRequest() {
        when(userService.login(null))
            .thenThrow(new IllegalArgumentException("Login request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.login(null);
        });
    }

    /**
     * Test login with invalid credentials.
     * Verifies handling of incorrect username or password.
     */
    @Test
    void testLogin_InvalidCredentials() {
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> {
            userController.login(loginRequest);
        });
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with empty username.
     * Verifies validation of required username field.
     */
    @Test
    void testLogin_EmptyUsername() {
        LoginRequest emptyUsernameRequest = new LoginRequest();
        emptyUsernameRequest.setUsername("");
        emptyUsernameRequest.setPassword("password123");

        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Username cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.login(emptyUsernameRequest);
        });
    }

    /**
     * Test login with empty password.
     * Verifies validation of required password field.
     */
    @Test
    void testLogin_EmptyPassword() {
        LoginRequest emptyPasswordRequest = new LoginRequest();
        emptyPasswordRequest.setUsername("testuser");
        emptyPasswordRequest.setPassword("");

        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Password cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.login(emptyPasswordRequest);
        });
    }

    /**
     * Test successfully retrieving user profile.
     * Verifies that user profile is returned correctly.
     */
    @Test
    void testGetProfile_Success() {
        when(userService.getProfile(any(UUID.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getProfile(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals("testuser", response.getBody().getUsername());
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

        assertThrows(IllegalArgumentException.class, () -> {
            userController.getProfile(null);
        });
    }

    /**
     * Test retrieving profile for non-existent user.
     * Verifies handling of invalid user ID.
     */
    @Test
    void testGetProfile_UserNotFound() {
        when(userService.getProfile(any(UUID.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.getProfile(userId);
        });
        verify(userService, times(1)).getProfile(eq(userId));
    }

    /**
     * Test successfully updating user profile.
     * Verifies that profile is updated and returns updated data.
     */
    @Test
    void testUpdateProfile_Success() {
        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setUserId(userId);
        updatedResponse.setUsername("testuser");
        updatedResponse.setEmail("updated@example.com");
        updatedResponse.setFullName("Updated Name");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenReturn(updatedResponse);

        ResponseEntity<UserResponse> response = userController.updateProfile(userId, updateProfileRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getFullName());
        assertEquals("updated@example.com", response.getBody().getEmail());
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with null user ID.
     * Verifies proper handling of null user ID.
     */
    @Test
    void testUpdateProfile_NullUserId() {
        when(userService.updateProfile(null, updateProfileRequest))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(null, updateProfileRequest);
        });
    }

    /**
     * Test updating profile with null request.
     * Verifies proper handling of null update request.
     */
    @Test
    void testUpdateProfile_NullRequest() {
        when(userService.updateProfile(any(UUID.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Update request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(userId, null);
        });
    }

    /**
     * Test updating profile for non-existent user.
     * Verifies handling of invalid user ID.
     */
    @Test
    void testUpdateProfile_UserNotFound() {
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with invalid email.
     * Verifies validation of email format during update.
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
     * Test updating profile with duplicate email.
     * Verifies handling when updated email already exists.
     */
    @Test
    void testUpdateProfile_DuplicateEmail() {
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with empty fields.
     * Verifies handling of empty update fields.
     */
    @Test
    void testUpdateProfile_EmptyFields() {
        UpdateProfileRequest emptyRequest = new UpdateProfileRequest();
        emptyRequest.setFullName("");
        emptyRequest.setEmail("");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new IllegalArgumentException("Fields cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(userId, emptyRequest);
        });
    }
}