package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserController
 * Tests user management operations including signup, login, profile retrieval and update
 */
@ExtendWith(MockitoExtension.class)
class test_UserController {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        testUserId = UUID.randomUUID();
    }

    /**
     * Test user signup successfully
     * Verifies that a valid signup request returns HTTP 201 with user response
     */
    @Test
    void testSignup_Success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setFullName("New User");

        UserResponse response = new UserResponse();
        response.setId(testUserId);
        response.setUsername("newuser");
        response.setEmail("newuser@example.com");

        when(userService.signup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with invalid email
     * Verifies that invalid email format is rejected
     */
    @Test
    void testSignup_InvalidEmail() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("invalid-email");
        request.setPassword("Password123!");
        request.setFullName("New User");

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with weak password
     * Verifies that weak password is rejected
     */
    @Test
    void testSignup_WeakPassword() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("weak");
        request.setFullName("New User");

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(SignupRequest.class));
    }

    /**
     * Test signup with duplicate username
     * Verifies that duplicate username is rejected
     */
    @Test
    void testSignup_DuplicateUsername() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("existinguser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setFullName("New User");

        when(userService.signup(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).signup(any(SignupRequest.class));
    }

    /**
     * Test user login successfully
     * Verifies that valid credentials return HTTP 200 with user response
     */
    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Password123!");

        UserResponse response = new UserResponse();
        response.setId(testUserId);
        response.setUsername("testuser");
        response.setEmail("testuser@example.com");

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies that invalid credentials are rejected
     */
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Test login with empty username
     * Verifies that empty username is rejected
     */
    @Test
    void testLogin_EmptyUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    /**
     * Test getting user profile successfully
     * Verifies that authenticated user can retrieve their profile
     */
    @Test
    void testGetProfile_Success() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(testUserId);
        response.setUsername("testuser");
        response.setEmail("testuser@example.com");
        response.setFullName("Test User");

        when(userService.getProfile(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get("/api/users/profile")
                .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));

        verify(userService, times(1)).getProfile(testUserId);
    }

    /**
     * Test getting profile without user ID header
     * Verifies that missing user ID header is rejected
     */
    @Test
    void testGetProfile_MissingUserId() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getProfile(any(UUID.class));
    }

    /**
     * Test getting profile with invalid user ID
     * Verifies that invalid UUID format is rejected
     */
    @Test
    void testGetProfile_InvalidUserId() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getProfile(any(UUID.class));
    }

    /**
     * Test updating user profile successfully
     * Verifies that authenticated user can update their profile
     */
    @Test
    void testUpdateProfile_Success() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        UserResponse response = new UserResponse();
        response.setId(testUserId);
        response.setUsername("testuser");
        response.setEmail("updated@example.com");
        response.setFullName("Updated Name");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/profile")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with invalid email
     * Verifies that invalid email format is rejected
     */
    @Test
    void testUpdateProfile_InvalidEmail() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/profile")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile without user ID
     * Verifies that missing user ID header is rejected
     */
    @Test
    void testUpdateProfile_MissingUserId() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }

    /**
     * Test updating profile with service exception
     * Verifies proper error handling when user service fails
     */
    @Test
    void testUpdateProfile_ServiceException() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/api/users/profile")
                .header("X-User-Id", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).updateProfile(any(UUID.class), any(UpdateProfileRequest.class));
    }
}