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
 * Unit test class for UserController
 * Tests user operations including signup, login, profile retrieval and update
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
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setFullName("Updated Name");
        updateProfileRequest.setEmail("updated@example.com");

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("Test User");
    }

    /**
     * Test successful user signup
     */
    @Test
    void testSignup_Success() {
        when(userService.signup(any(SignupRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.signup(signupRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userResponse.getUsername(), response.getBody().getUsername());
        assertEquals(userResponse.getEmail(), response.getBody().getEmail());
        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with null request
     */
    @Test
    void testSignup_NullRequest() {
        when(userService.signup(any()))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(null);
        });
    }

    /**
     * Test signup with duplicate username
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
     * Test signup with duplicate email
     */
    @Test
    void testSignup_DuplicateEmail() {
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        assertThrows(RuntimeException.class, () -> {
            userController.signup(signupRequest);
        });
    }

    /**
     * Test signup with invalid email format
     */
    @Test
    void testSignup_InvalidEmail() {
        signupRequest.setEmail("invalid-email");
        when(userService.signup(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.signup(signupRequest);
        });
    }

    /**
     * Test successful user login
     */
    @Test
    void testLogin_Success() {
        when(userService.login(any(LoginRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userResponse.getUsername(), response.getBody().getUsername());
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with invalid credentials
     */
    @Test
    void testLogin_InvalidCredentials() {
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid username or password"));

        assertThrows(RuntimeException.class, () -> {
            userController.login(loginRequest);
        });
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with null request
     */
    @Test
    void testLogin_NullRequest() {
        when(userService.login(any()))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.login(null);
        });
    }

    /**
     * Test login with non-existent user
     */
    @Test
    void testLogin_UserNotFound() {
        when(userService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.login(loginRequest);
        });
    }

    /**
     * Test successfully retrieving user profile
     */
    @Test
    void testGetProfile_Success() {
        when(userService.getProfile(any(UUID.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getProfile(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals(userResponse.getUsername(), response.getBody().getUsername());
        verify(userService, times(1)).getProfile(eq(userId));
    }

    /**
     * Test retrieving profile with null user ID
     */
    @Test
    void testGetProfile_NullUserId() {
        when(userService.getProfile(any()))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.getProfile(null);
        });
    }

    /**
     * Test retrieving profile for non-existent user
     */
    @Test
    void testGetProfile_UserNotFound() {
        when(userService.getProfile(any(UUID.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.getProfile(userId);
        });
    }

    /**
     * Test successfully updating user profile
     */
    @Test
    void testUpdateProfile_Success() {
        userResponse.setFullName(updateProfileRequest.getFullName());
        userResponse.setEmail(updateProfileRequest.getEmail());
        
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.updateProfile(userId, updateProfileRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updateProfileRequest.getFullName(), response.getBody().getFullName());
        assertEquals(updateProfileRequest.getEmail(), response.getBody().getEmail());
        verify(userService, times(1)).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with null request
     */
    @Test
    void testUpdateProfile_NullRequest() {
        when(userService.updateProfile(any(UUID.class), any()))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(userId, null);
        });
    }

    /**
     * Test updating profile with null user ID
     */
    @Test
    void testUpdateProfile_NullUserId() {
        when(userService.updateProfile(any(), any(UpdateProfileRequest.class)))
            .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(null, updateProfileRequest);
        });
    }

    /**
     * Test updating profile for non-existent user
     */
    @Test
    void testUpdateProfile_UserNotFound() {
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
    }

    /**
     * Test updating profile with invalid email
     */
    @Test
    void testUpdateProfile_InvalidEmail() {
        updateProfileRequest.setEmail("invalid-email");
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        assertThrows(IllegalArgumentException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
    }

    /**
     * Test updating profile with duplicate email
     */
    @Test
    void testUpdateProfile_DuplicateEmail() {
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        assertThrows(RuntimeException.class, () -> {
            userController.updateProfile(userId, updateProfileRequest);
        });
    }
}