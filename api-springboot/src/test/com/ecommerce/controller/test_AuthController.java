package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test class for AuthController
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

    private UserRegistrationDTO registrationDTO;
    private UserDTO userDTO;
    private LoginRequestDTO loginRequestDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("testuser");
        loginRequestDTO.setPassword("password123");

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken("jwt-token");
        loginResponseDTO.setUser(userDTO);
    }

    /**
     * Test successful user registration
     * Verifies that a new user can be registered successfully
     */
    @Test
    void testRegister_Success() {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        ResponseEntity<ApiResponseDTO<UserDTO>> response = authController.register(registrationDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully", response.getBody().getMessage());
        assertEquals(userDTO, response.getBody().getData());
        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test registration with null registration DTO
     * Verifies proper handling of null input
     */
    @Test
    void testRegister_NullRegistrationDTO() {
        when(userService.registerUser(null)).thenThrow(new IllegalArgumentException("Registration data cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            authController.register(null);
        });
    }

    /**
     * Test successful user login
     * Verifies that a user can login successfully with valid credentials
     */
    @Test
    void testLogin_Success() {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        ResponseEntity<ApiResponseDTO<LoginResponseDTO>> response = authController.login(loginRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(loginResponseDTO, response.getBody().getData());
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with invalid credentials
     * Verifies proper error handling for invalid credentials
     */
    @Test
    void testLogin_InvalidCredentials() {
        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequestDTO);
        });
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with null login request
     * Verifies proper handling of null input
     */
    @Test
    void testLogin_NullLoginRequest() {
        when(authService.login(null)).thenThrow(new IllegalArgumentException("Login request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            authController.login(null);
        });
    }

    /**
     * Test successful user logout
     * Verifies that a user can logout successfully
     */
    @Test
    void testLogout_Success() {
        Long userId = 1L;
        when(authentication.getName()).thenReturn(userId.toString());
        doNothing().when(authService).logout(userId);

        ResponseEntity<ApiResponseDTO<Void>> response = authController.logout(authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(authService, times(1)).logout(userId);
    }

    /**
     * Test logout with invalid user ID
     * Verifies proper error handling for invalid user ID
     */
    @Test
    void testLogout_InvalidUserId() {
        when(authentication.getName()).thenReturn("invalid");

        assertThrows(NumberFormatException.class, () -> {
            authController.logout(authentication);
        });
    }

    /**
     * Test logout with null authentication
     * Verifies proper handling of null authentication
     */
    @Test
    void testLogout_NullAuthentication() {
        assertThrows(NullPointerException.class, () -> {
            authController.logout(null);
        });
    }
}