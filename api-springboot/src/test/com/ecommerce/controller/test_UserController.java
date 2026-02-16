package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserController
 * 
 * This test class verifies the REST API endpoints for user management.
 * It tests:
 * - User registration (signup)
 * - User authentication (login)
 * - User profile retrieval
 * - User profile update
 * - Input validation
 * - Error handling
 * 
 * @author Test Generation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Test Suite")
class test_UserController {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserPrincipal userPrincipal;
    private UUID userId;

    /**
     * Setup method executed before each test
     * Initializes test data and MockMvc instance
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        
        userId = UUID.randomUUID();
        userPrincipal = new UserPrincipal(
            userId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );
    }

    /**
     * Test successful user registration
     * 
     * Verifies that:
     * - HTTP 201 Created status is returned
     * - User response contains correct data
     * - Service method is called with correct parameters
     */
    @Test
    @DisplayName("Should successfully register new user")
    void testSignup_Success() throws Exception {
        // Arrange
        UserSignupRequest request = UserSignupRequest.builder()
            .username("newuser")
            .password("password123")
            .fullName("New User")
            .email("newuser@example.com")
            .build();

        UserResponse response = UserResponse.builder()
            .id(userId)
            .username("newuser")
            .fullName("New User")
            .email("newuser@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(false)
            .build();

        when(userService.signup(any(UserSignupRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).signup(any(UserSignupRequest.class));
    }

    /**
     * Test user registration with invalid data
     * 
     * Verifies that:
     * - HTTP 400 Bad Request status is returned
     * - Validation errors are returned
     */
    @Test
    @DisplayName("Should fail registration with invalid username")
    void testSignup_InvalidUsername() throws Exception {
        // Arrange
        UserSignupRequest request = UserSignupRequest.builder()
            .username("ab") // Too short
            .password("password123")
            .fullName("New User")
            .email("newuser@example.com")
            .build();

        // Act & Assert
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any());
    }

    /**
     * Test user registration with invalid email
     * 
     * Verifies that:
     * - HTTP 400 Bad Request status is returned
     * - Email validation error is returned
     */
    @Test
    @DisplayName("Should fail registration with invalid email")
    void testSignup_InvalidEmail() throws Exception {
        // Arrange
        UserSignupRequest request = UserSignupRequest.builder()
            .username("newuser")
            .password("password123")
            .fullName("New User")
            .email("invalid-email")
            .build();

        // Act & Assert
        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any());
    }

    /**
     * Test successful user login
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - JWT token is returned
     * - User details are returned
     */
    @Test
    @DisplayName("Should successfully login user")
    void testLogin_Success() throws Exception {
        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
            .username("testuser")
            .password("password123")
            .build();

        UserResponse userResponse = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(true)
            .build();

        AuthResponse authResponse = AuthResponse.builder()
            .token("jwt-token-here")
            .type("Bearer")
            .user(userResponse)
            .build();

        when(userService.login(any(UserLoginRequest.class)))
            .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-here"))
            .andExpect(jsonPath("$.type").value("Bearer"))
            .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(userService, times(1)).login(any(UserLoginRequest.class));
    }

    /**
     * Test login with missing credentials
     * 
     * Verifies that:
     * - HTTP 400 Bad Request status is returned
     * - Validation errors are returned
     */
    @Test
    @DisplayName("Should fail login with missing credentials")
    void testLogin_MissingCredentials() throws Exception {
        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
            .username("")
            .password("")
            .build();

        // Act & Assert
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).login(any());
    }

    /**
     * Test successful profile retrieval
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - User profile data is returned
     * - Service method is called with correct user ID
     */
    @Test
    @DisplayName("Should successfully retrieve user profile")
    void testGetProfile_Success() throws Exception {
        // Arrange
        UserResponse response = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(true)
            .build();

        when(userService.getProfile(userId))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/users/profile")
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getProfile(userId);
    }

    /**
     * Test successful profile update
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Updated user data is returned
     * - Service method is called with correct parameters
     */
    @Test
    @DisplayName("Should successfully update user profile")
    void testUpdateProfile_Success() throws Exception {
        // Arrange
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email("updated@example.com")
            .build();

        UserResponse response = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .fullName("Updated Name")
            .email("updated@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(true)
            .build();

        when(userService.updateProfile(eq(userId), any(UserProfileUpdateRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Name"))
            .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateProfile(eq(userId), any(UserProfileUpdateRequest.class));
    }

    /**
     * Test profile update with invalid email
     * 
     * Verifies that:
     * - HTTP 400 Bad Request status is returned
     * - Validation error is returned
     */
    @Test
    @DisplayName("Should fail profile update with invalid email")
    void testUpdateProfile_InvalidEmail() throws Exception {
        // Arrange
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email("invalid-email")
            .build();

        // Act & Assert
        mockMvc.perform(put("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> userPrincipal.getUsername()))
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(), any());
    }

    /**
     * Test profile operations without authentication
     * 
     * Verifies that:
     * - HTTP 401 Unauthorized status is returned
     * - No service methods are called
     */
    @Test
    @DisplayName("Should fail profile operations without authentication")
    void testProfileOperations_NoAuthentication() throws Exception {
        // Act & Assert - Get Profile
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isUnauthorized());

        // Act & Assert - Update Profile
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email("updated@example.com")
            .build();

        mockMvc.perform(put("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(userService, never()).getProfile(any());
        verify(userService, never()).updateProfile(any(), any());
    }
}