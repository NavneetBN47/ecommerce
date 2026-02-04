package com.example.ecommerce.controller;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserController.
 * Tests user registration, authentication, and profile management endpoints.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserResponse userResponse;
    private UpdateProfileRequest updateProfileRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        signUpRequest = new SignUpRequest(
                "testuser",
                "password123",
                "John Doe",
                "test@example.com"
        );

        loginRequest = new LoginRequest(
                "testuser",
                "password123"
        );

        userResponse = new UserResponse(
                userId,
                "testuser",
                "John Doe",
                "test@example.com",
                LocalDateTime.now()
        );

        loginResponse = new LoginResponse(
                "jwt.token.here",
                "testuser",
                "Login successful"
        );

        updateProfileRequest = new UpdateProfileRequest(
                "Jane Smith",
                "jane@example.com"
        );
    }

    /**
     * Test successful user registration.
     */
    @Test
    void signUp_ValidRequest_ShouldReturnUserResponse() throws Exception {
        // Given
        when(userService.signUp(any(SignUpRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(userService).signUp(any(SignUpRequest.class));
    }

    /**
     * Test user registration with invalid data.
     */
    @Test
    void signUp_InvalidRequest_ShouldReturn400() throws Exception {
        // Given
        SignUpRequest invalidRequest = new SignUpRequest(
                "", // Invalid: empty username
                "123", // Invalid: too short password
                "", // Invalid: empty full name
                "invalid-email" // Invalid: malformed email
        );

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    /**
     * Test successful user login.
     */
    @Test
    void login_ValidCredentials_ShouldReturnLoginResponse() throws Exception {
        // Given
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(userService).login(any(LoginRequest.class));
    }

    /**
     * Test login with missing fields.
     */
    @Test
    void login_MissingFields_ShouldReturn400() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest(
                "", // Missing username
                "" // Missing password
        );

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    /**
     * Test successful profile retrieval.
     */
    @Test
    void getProfile_AuthenticatedUser_ShouldReturnUserResponse() throws Exception {
        // Given
        when(userService.getProfile(any(UUID.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .requestAttr("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(userService).getProfile(userId);
    }

    /**
     * Test successful profile update.
     */
    @Test
    void updateProfile_ValidRequest_ShouldReturnUpdatedUserResponse() throws Exception {
        // Given
        UserResponse updatedUserResponse = new UserResponse(
                userId,
                "testuser",
                "Jane Smith",
                "jane@example.com",
                LocalDateTime.now()
        );

        when(userService.updateProfile(any(UUID.class), any(UpdateProfileRequest.class)))
                .thenReturn(updatedUserResponse);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.fullName").value("Jane Smith"));

        verify(userService).updateProfile(eq(userId), any(UpdateProfileRequest.class));
    }

    /**
     * Test profile update with invalid data.
     */
    @Test
    void updateProfile_InvalidRequest_ShouldReturn400() throws Exception {
        // Given
        UpdateProfileRequest invalidRequest = new UpdateProfileRequest(
                "", // Invalid: empty full name
                "invalid-email" // Invalid: malformed email
        );

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .requestAttr("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    /**
     * Test that all endpoints handle malformed JSON gracefully.
     */
    @Test
    void allEndpoints_MalformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{\"username\": \"test\", \"email\":}";

        // Test signup
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        // Test login
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    /**
     * Test signup with null values.
     */
    @Test
    void signUp_NullValues_ShouldReturn400() throws Exception {
        // Given
        SignUpRequest requestWithNulls = new SignUpRequest(
                null,
                null,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNulls)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    /**
     * Test login with null values.
     */
    @Test
    void login_NullValues_ShouldReturn400() throws Exception {
        // Given
        LoginRequest requestWithNulls = new LoginRequest(null, null);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNulls)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}