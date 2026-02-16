package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.UserPrincipal;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for UserController
 * 
 * This test class verifies the REST API endpoints for user management,
 * including signup, login, profile retrieval, and profile updates.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_UserController {

    @Mock
    private UserService userService;

    @Mock
    private UserPrincipal currentUser;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);

        userResponse = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .emailVerified(false)
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token-123")
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    /**
     * Test successful user signup
     * 
     * Verifies that a new user can be registered successfully and
     * returns HTTP 201 CREATED with user details.
     */
    @Test
    void signup_WithValidRequest_ShouldReturnCreatedWithUserResponse() {
        UserSignupRequest request = new UserSignupRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setEmail("test@example.com");

        when(userService.signup(any(UserSignupRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.signup(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
        verify(userService, times(1)).signup(any(UserSignupRequest.class));
    }

    /**
     * Test signup with all required fields
     * 
     * Verifies that signup requires and properly processes all
     * mandatory user fields.
     */
    @Test
    void signup_WithAllRequiredFields_ShouldCreateUserSuccessfully() {
        UserSignupRequest request = new UserSignupRequest();
        request.setUsername("newuser");
        request.setPassword("securePassword123");
        request.setFullName("New User");
        request.setEmail("newuser@example.com");

        when(userService.signup(any(UserSignupRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.signup(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsActive()).isTrue();
        assertThat(response.getBody().getEmailVerified()).isFalse();
    }

    /**
     * Test successful user login
     * 
     * Verifies that a user can login successfully and
     * returns HTTP 200 OK with JWT token and user details.
     */
    @Test
    void login_WithValidCredentials_ShouldReturnOkWithAuthResponse() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userService.login(any(UserLoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getBody().getType()).isEqualTo("Bearer");
        assertThat(response.getBody().getUser()).isNotNull();
        verify(userService, times(1)).login(any(UserLoginRequest.class));
    }

    /**
     * Test login returns JWT token
     * 
     * Verifies that successful login returns a valid JWT token
     * in the authentication response.
     */
    @Test
    void login_ShouldReturnJwtToken() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userService.login(any(UserLoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.login(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotEmpty();
        assertThat(response.getBody().getType()).isEqualTo("Bearer");
    }

    /**
     * Test successful profile retrieval
     * 
     * Verifies that an authenticated user can retrieve their profile
     * and returns HTTP 200 OK with user details.
     */
    @Test
    void getProfile_WithAuthenticatedUser_ShouldReturnOkWithUserResponse() {
        when(userService.getProfile(userId)).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getProfile(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        verify(userService, times(1)).getProfile(userId);
    }

    /**
     * Test profile retrieval returns complete user data
     * 
     * Verifies that the profile endpoint returns all user fields
     * including timestamps and status flags.
     */
    @Test
    void getProfile_ShouldReturnCompleteUserData() {
        when(userService.getProfile(userId)).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.getProfile(currentUser);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUsername()).isNotNull();
        assertThat(response.getBody().getFullName()).isNotNull();
        assertThat(response.getBody().getEmail()).isNotNull();
        assertThat(response.getBody().getCreatedAt()).isNotNull();
        assertThat(response.getBody().getIsActive()).isNotNull();
        assertThat(response.getBody().getEmailVerified()).isNotNull();
    }

    /**
     * Test successful profile update
     * 
     * Verifies that an authenticated user can update their profile
     * and returns HTTP 200 OK with updated user details.
     */
    @Test
    void updateProfile_WithValidRequest_ShouldReturnOkWithUpdatedUser() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        UserResponse updatedUser = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .fullName("Updated Name")
                .email("updated@example.com")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .emailVerified(false)
                .build();

        when(userService.updateProfile(eq(userId), any(UserProfileUpdateRequest.class)))
                .thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateProfile(currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getEmail()).isEqualTo("updated@example.com");
        verify(userService, times(1)).updateProfile(eq(userId), any(UserProfileUpdateRequest.class));
    }

    /**
     * Test profile update with only name change
     * 
     * Verifies that a user can update only their full name
     * while keeping other fields unchanged.
     */
    @Test
    void updateProfile_WithOnlyNameChange_ShouldUpdateNameOnly() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("New Name");
        request.setEmail("test@example.com");

        UserResponse updatedUser = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .fullName("New Name")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .emailVerified(false)
                .build();

        when(userService.updateProfile(eq(userId), any(UserProfileUpdateRequest.class)))
                .thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateProfile(currentUser, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("New Name");
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }

    /**
     * Test profile update with only email change
     * 
     * Verifies that a user can update only their email
     * while keeping other fields unchanged.
     */
    @Test
    void updateProfile_WithOnlyEmailChange_ShouldUpdateEmailOnly() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Test User");
        request.setEmail("newemail@example.com");

        UserResponse updatedUser = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .fullName("Test User")
                .email("newemail@example.com")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .emailVerified(false)
                .build();

        when(userService.updateProfile(eq(userId), any(UserProfileUpdateRequest.class)))
                .thenReturn(updatedUser);

        ResponseEntity<UserResponse> response = userController.updateProfile(currentUser, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("Test User");
        assertThat(response.getBody().getEmail()).isEqualTo("newemail@example.com");
    }
}