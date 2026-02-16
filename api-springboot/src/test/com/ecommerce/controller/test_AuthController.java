package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthController
 * Tests authentication operations including registration, login, and logout
 */
@ExtendWith(MockitoExtension.class)
class test_AuthController {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Test successful user registration
     * Verifies that a valid registration request returns HTTP 201 with user data
     */
    @Test
    void testRegister_Success() throws Exception {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("Password123!");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");

        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test registration with invalid data
     * Verifies that invalid registration data is rejected
     */
    @Test
    void testRegister_InvalidData() throws Exception {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("");
        registrationDTO.setEmail("invalid-email");
        registrationDTO.setPassword("weak");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test successful user login
     * Verifies that valid credentials return HTTP 200 with JWT token
     */
    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("Password123!");

        LoginResponseDTO loginResponse = new LoginResponseDTO();
        loginResponse.setToken("jwt-token-123");
        loginResponse.setUserId(1L);
        loginResponse.setUsername("testuser");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-123"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies that invalid credentials are rejected
     */
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with empty identifier
     * Verifies that empty identifier is rejected
     */
    @Test
    void testLogin_EmptyIdentifier() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    /**
     * Test successful logout
     * Verifies that authenticated user can logout successfully
     */
    @Test
    void testLogout_Success() throws Exception {
        when(authentication.getName()).thenReturn("1");
        doNothing().when(authService).logout(anyLong());

        mockMvc.perform(post("/api/auth/logout")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService, times(1)).logout(1L);
    }

    /**
     * Test logout without authentication
     * Verifies that unauthenticated logout request is rejected
     */
    @Test
    void testLogout_NoAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).logout(anyLong());
    }

    /**
     * Test logout with service exception
     * Verifies proper error handling when logout service fails
     */
    @Test
    void testLogout_ServiceException() throws Exception {
        when(authentication.getName()).thenReturn("1");
        doThrow(new RuntimeException("Service error")).when(authService).logout(anyLong());

        mockMvc.perform(post("/api/auth/logout")
                .principal(authentication))
                .andExpect(status().isInternalServerError());

        verify(authService, times(1)).logout(1L);
    }
}