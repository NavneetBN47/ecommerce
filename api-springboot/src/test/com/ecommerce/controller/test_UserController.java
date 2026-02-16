package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserController
 * 
 * Tests user management endpoints including:
 * - User registration (signup)
 * - User authentication (login)
 * - Profile retrieval
 * - Profile updates
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class test_UserController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserPrincipal testUser;
    private UUID testUserId;
    private UserResponse testUserResponse;
    private AuthResponse testAuthResponse;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new UserPrincipal(
            testUserId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );

        testUserResponse = UserResponse.builder()
            .id(testUserId)
            .username("testuser")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(false)
            .build();

        testAuthResponse = AuthResponse.builder()
            .token("test-jwt-token")
            .type("Bearer")
            .user(testUserResponse)
            .build();
    }

    /**
     * Test successful user signup
     * 
     * Validates:
     * - HTTP 201 Created status
     * - User details in response
     */
    @Test
    @DisplayName("Should register new user successfully")
    void testSignup_Success() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
            .username("newuser")
            .password("password123")
            .fullName("New User")
            .email("newuser@example.com")
            .build();

        when(userService.signup(any(UserSignupRequest.class)))
            .thenReturn(testUserResponse);

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Test signup with invalid data
     * 
     * Validates:
     * - HTTP 400 Bad Request for validation failures
     */
    @Test
    @DisplayName("Should return 400 for invalid signup data")
    void testSignup_InvalidData() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
            .username("ab")
            .password("123")
            .fullName("")
            .email("invalid-email")
            .build();

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test successful user login
     * 
     * Validates:
     * - HTTP 200 OK status
     * - JWT token in response
     * - User details in response
     */
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
            .username("testuser")
            .password("password123")
            .build();

        when(userService.login(any(UserLoginRequest.class)))
            .thenReturn(testAuthResponse);

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("test-jwt-token"))
            .andExpect(jsonPath("$.type").value("Bearer"))
            .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    /**
     * Test login with missing credentials
     * 
     * Validates:
     * - HTTP 400 Bad Request for missing fields
     */
    @Test
    @DisplayName("Should return 400 for missing login credentials")
    void testLogin_MissingCredentials() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
            .username("")
            .password("")
            .build();

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test getting user profile
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Profile details in response
     */
    @Test
    @WithMockUser
    @DisplayName("Should get user profile successfully")
    void testGetProfile_Success() throws Exception {
        when(userService.getProfile(any(UUID.class)))
            .thenReturn(testUserResponse);

        mockMvc.perform(get("/users/profile")
                .with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Test getting profile without authentication
     * 
     * Validates:
     * - HTTP 401 Unauthorized
     */
    @Test
    @DisplayName("Should return 401 for unauthenticated profile request")
    void testGetProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Test updating user profile
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Updated profile details
     */
    @Test
    @WithMockUser
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_Success() throws Exception {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email("updated@example.com")
            .build();

        UserResponse updatedResponse = UserResponse.builder()
            .id(testUserId)
            .username("testuser")
            .fullName("Updated Name")
            .email("updated@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(false)
            .build();

        when(userService.updateProfile(any(UUID.class), any(UserProfileUpdateRequest.class)))
            .thenReturn(updatedResponse);

        mockMvc.perform(put("/users/profile")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Name"))
            .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    /**
     * Test updating profile with invalid data
     * 
     * Validates:
     * - HTTP 400 Bad Request for validation failures
     */
    @Test
    @WithMockUser
    @DisplayName("Should return 400 for invalid profile update data")
    void testUpdateProfile_InvalidData() throws Exception {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .fullName("")
            .email("invalid-email")
            .build();

        mockMvc.perform(put("/users/profile")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}