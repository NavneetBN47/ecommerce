package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for UserController
 * Tests user management operations including signup, login, profile retrieval and updates
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class test_UserController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private UserResponse userResponse;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setFirstName("Updated");
        updateProfileRequest.setLastName("Name");
        updateProfileRequest.setEmail("updated@example.com");

        userResponse = new UserResponse();
        userResponse.setId(testUserId);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("Test");
        userResponse.setLastName("User");
    }

    /**
     * Test successful user signup
     * Verifies that valid signup request returns 201 CREATED with user data
     */
    @Test
    @DisplayName("Should signup user successfully")
    void testSignup_Success() throws Exception {
        when(userService.signup(any(SignupRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));

        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with invalid data
     * Verifies that validation errors are properly handled
     */
    @Test
    @DisplayName("Should return 400 when signup data is invalid")
    void testSignup_InvalidData() throws Exception {
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername("");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("weak");

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with duplicate username
     * Verifies that duplicate username error is handled
     */
    @Test
    @DisplayName("Should return error when username already exists")
    void testSignup_DuplicateUsername() throws Exception {
        when(userService.signup(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test successful user login
     * Verifies that valid credentials return user data
     */
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies that authentication failures are properly handled
     */
    @Test
    @DisplayName("Should return error with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with empty username
     * Verifies that validation catches empty username
     */
    @Test
    @DisplayName("Should return 400 when username is empty")
    void testLogin_EmptyUsername() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("password");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    /**
     * Test successfully retrieving user profile
     * Verifies that profile retrieval returns user data
     */
    @Test
    @DisplayName("Should retrieve user profile successfully")
    void testGetProfile_Success() throws Exception {
        when(userService.getProfile(any(UUID.class))).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/profile")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getProfile(testUserId);
    }

    /**
     * Test retrieving profile without user ID header
     * Verifies that missing user ID is handled
     */
    @Test
    @DisplayName("Should return 400 when X-User-Id header is missing")
    void testGetProfile_MissingUserId() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getProfile(any(UUID.class));
    }

    /**
     * Test retrieving profile for non-existent user
     * Verifies that service exception is properly handled
     */
    @Test
    @DisplayName("Should return error when user not found")
    void testGetProfile_UserNotFound() throws Exception {
        when(userService.getProfile(any(UUID.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/profile")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).getProfile(testUserId);
    }

    /**
     * Test successfully updating user profile
     * Verifies that valid update request returns updated user data
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_Success() throws Exception {
        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setId(testUserId);
        updatedResponse.setUsername("testuser");
        updatedResponse.setEmail("updated@example.com");
        updatedResponse.setFirstName("Updated");
        updatedResponse.setLastName("Name");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/profile")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with invalid data
     * Verifies that validation errors are properly handled
     */
    @Test
    @DisplayName("Should return 400 when update data is invalid")
    void testUpdateProfile_InvalidData() throws Exception {
        UpdateProfileRequest invalidRequest = new UpdateProfileRequest();
        invalidRequest.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/profile")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile without user ID
     * Verifies that missing user ID is handled
     */
    @Test
    @DisplayName("Should return 400 when updating profile without user ID")
    void testUpdateProfile_MissingUserId() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with service exception
     * Verifies that service exceptions are properly handled
     */
    @Test
    @DisplayName("Should handle service exception during profile update")
    void testUpdateProfile_ServiceException() throws Exception {
        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/api/users/profile")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }
}