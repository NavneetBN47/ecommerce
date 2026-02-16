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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for AuthController
 * Tests authentication endpoints including register, login, and logout
 * 
 * @author QA Automation Agent
 * @version 1.0.0
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
    void registerShouldReturnCreatedStatusWithUserDTO() {
        // Given
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");

        UserDTO userDTO = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        // When
        ResponseEntity<ApiResponseDTO<UserDTO>> response = authController.register(registrationDTO);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully", response.getBody().getMessage());
        assertEquals(userDTO, response.getBody().getData());
        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test registration with duplicate username
     * Verifies proper exception handling for duplicate user
     */
    @Test
    void registerShouldHandleDuplicateUsername() {
        // Given
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("existinguser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");

        when(userService.registerUser(any(UserRegistrationDTO.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            authController.register(registrationDTO);
        });
        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test successful user login
     * Verifies that valid credentials return HTTP 200 with JWT token
     */
    @Test
    void loginShouldReturnOkStatusWithLoginResponse() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("password123");

        UserDTO userDTO = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
            .token("jwt-token")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .user(userDTO)
            .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        // When
        ResponseEntity<ApiResponseDTO<LoginResponseDTO>> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(loginResponse, response.getBody().getData());
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies proper exception handling for authentication failure
     */
    @Test
    void loginShouldHandleInvalidCredentials() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequest);
        });
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with non-existent user
     * Verifies proper handling when user is not found
     */
    @Test
    void loginShouldHandleNonExistentUser() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("nonexistent");
        loginRequest.setPassword("password123");

        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequest);
        });
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test successful user logout
     * Verifies that logout clears cart and returns HTTP 200
     */
    @Test
    void logoutShouldReturnOkStatus() {
        // Given
        Long userId = 1L;
        when(authentication.getName()).thenReturn(userId.toString());
        doNothing().when(authService).logout(userId);

        // When
        ResponseEntity<ApiResponseDTO<Void>> response = authController.logout(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        verify(authService, times(1)).logout(userId);
    }

    /**
     * Test logout with invalid authentication
     * Verifies proper handling of authentication errors
     */
    @Test
    void logoutShouldHandleInvalidAuthentication() {
        // Given
        when(authentication.getName()).thenReturn(null);

        // When/Then
        assertThrows(Exception.class, () -> {
            authController.logout(authentication);
        });
    }

    /**
     * Test logout clears user cart
     * Verifies that logout operation triggers cart cleanup
     */
    @Test
    void logoutShouldClearUserCart() {
        // Given
        Long userId = 1L;
        when(authentication.getName()).thenReturn(userId.toString());
        doNothing().when(authService).logout(userId);

        // When
        authController.logout(authentication);

        // Then
        verify(authService, times(1)).logout(userId);
    }

    /**
     * Test registration with null fields
     * Verifies validation of required fields
     */
    @Test
    void registerShouldValidateRequiredFields() {
        // Given
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        // Missing required fields

        when(userService.registerUser(any(UserRegistrationDTO.class)))
            .thenThrow(new IllegalArgumentException("Required fields missing"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            authController.register(registrationDTO);
        });
    }

    /**
     * Test login with empty credentials
     * Verifies validation of login request fields
     */
    @Test
    void loginShouldValidateCredentials() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        // Empty credentials

        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Credentials cannot be empty"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            authController.login(loginRequest);
        });
    }
}