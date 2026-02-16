package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for AuthController
 * Tests authentication operations including registration, login, and logout
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class test_AuthController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationDTO validRegistrationDTO;
    private LoginRequestDTO validLoginRequest;
    private UserDTO userDTO;
    private LoginResponseDTO loginResponseDTO;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setUsername("testuser");
        validRegistrationDTO.setEmail("test@example.com");
        validRegistrationDTO.setPassword("Password123!");
        validRegistrationDTO.setFirstName("Test");
        validRegistrationDTO.setLastName("User");

        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setIdentifier("testuser");
        validLoginRequest.setPassword("Password123!");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken("jwt-token-123");
        loginResponseDTO.setUser(userDTO);
    }

    /**
     * Test successful user registration
     * Verifies that a valid registration request returns 201 CREATED with user data
     */
    @Test
    @DisplayName("Should register user successfully with valid data")
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test registration with invalid data
     * Verifies that validation errors are properly handled
     */
    @Test
    @DisplayName("Should return 400 when registration data is invalid")
    void testRegisterUser_InvalidData() throws Exception {
        UserRegistrationDTO invalidDTO = new UserRegistrationDTO();
        invalidDTO.setUsername("");
        invalidDTO.setEmail("invalid-email");
        invalidDTO.setPassword("weak");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test registration with duplicate username
     * Verifies that duplicate username errors are handled
     */
    @Test
    @DisplayName("Should return error when username already exists")
    void testRegisterUser_DuplicateUsername() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test successful login
     * Verifies that valid credentials return JWT token and user data
     */
    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies that authentication failures are properly handled
     */
    @Test
    @DisplayName("Should return error with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with empty identifier
     * Verifies that validation catches empty identifier
     */
    @Test
    @DisplayName("Should return 400 when identifier is empty")
    void testLogin_EmptyIdentifier() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO();
        invalidRequest.setIdentifier("");
        invalidRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    /**
     * Test successful logout
     * Verifies that authenticated user can logout successfully
     */
    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should logout successfully for authenticated user")
    void testLogout_Success() throws Exception {
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("1");
        doNothing().when(authService).logout(anyLong());

        mockMvc.perform(post("/api/auth/logout")
                .with(authentication(mockAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService, times(1)).logout(1L);
    }

    /**
     * Test logout without authentication
     * Verifies that unauthenticated requests are rejected
     */
    @Test
    @DisplayName("Should return 401 when logout without authentication")
    void testLogout_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).logout(anyLong());
    }

    /**
     * Test logout with service exception
     * Verifies that service layer exceptions are properly handled
     */
    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should handle service exception during logout")
    void testLogout_ServiceException() throws Exception {
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("1");
        doThrow(new RuntimeException("Service error")).when(authService).logout(anyLong());

        mockMvc.perform(post("/api/auth/logout")
                .with(authentication(mockAuth)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).logout(1L);
    }

    /**
     * Test registration with null request body
     * Verifies that null request body is handled
     */
    @Test
    @DisplayName("Should return 400 when registration request body is null")
    void testRegisterUser_NullRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test login with null request body
     * Verifies that null request body is handled
     */
    @Test
    @DisplayName("Should return 400 when login request body is null")
    void testLogin_NullRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }
}