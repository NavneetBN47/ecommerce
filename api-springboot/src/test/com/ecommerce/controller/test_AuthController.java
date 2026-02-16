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
 * JUnit 5 test class for AuthController.
 * Tests authentication operations including registration, login, and logout.
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
    private LoginRequestDTO loginRequestDTO;
    private UserDTO userDTO;
    private LoginResponseDTO loginResponseDTO;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("testuser");
        loginRequestDTO.setPassword("password123");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken("jwt-token-123");
        loginResponseDTO.setUser(userDTO);
    }

    /**
     * Test successful user registration.
     * Verifies that a new user can be registered and returns HTTP 201 status.
     */
    @Test
    void testRegister_Success() {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        ResponseEntity<ApiResponseDTO<UserDTO>> response = authController.register(registrationDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User registered successfully", response.getBody().getMessage());
        assertEquals(userDTO, response.getBody().getData());
        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test registration with null registration DTO.
     * Verifies proper handling of null input.
     */
    @Test
    void testRegister_NullDTO() {
        when(userService.registerUser(null)).thenThrow(new IllegalArgumentException("Registration data cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            authController.register(null);
        });
    }

    /**
     * Test registration with duplicate username.
     * Verifies that duplicate username is handled properly.
     */
    @Test
    void testRegister_DuplicateUsername() {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        assertThrows(RuntimeException.class, () -> {
            authController.register(registrationDTO);
        });
        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test successful user login.
     * Verifies that valid credentials result in successful login with JWT token.
     */
    @Test
    void testLogin_Success() {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        ResponseEntity<ApiResponseDTO<LoginResponseDTO>> response = authController.login(loginRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(loginResponseDTO, response.getBody().getData());
        assertNotNull(response.getBody().getData().getToken());
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with invalid credentials.
     * Verifies that invalid credentials are rejected.
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
     * Test login with null login request.
     * Verifies proper handling of null input.
     */
    @Test
    void testLogin_NullRequest() {
        when(authService.login(null)).thenThrow(new IllegalArgumentException("Login request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            authController.login(null);
        });
    }

    /**
     * Test login with empty identifier.
     * Verifies validation of required fields.
     */
    @Test
    void testLogin_EmptyIdentifier() {
        LoginRequestDTO emptyIdentifier = new LoginRequestDTO();
        emptyIdentifier.setIdentifier("");
        emptyIdentifier.setPassword("password123");

        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Identifier cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> {
            authController.login(emptyIdentifier);
        });
    }

    /**
     * Test successful user logout.
     * Verifies that authenticated user can logout successfully.
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
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(authService, times(1)).logout(userId);
    }

    /**
     * Test logout with null authentication.
     * Verifies proper handling of unauthenticated logout attempt.
     */
    @Test
    void testLogout_NullAuthentication() {
        assertThrows(NullPointerException.class, () -> {
            authController.logout(null);
        });
    }

    /**
     * Test logout with invalid user ID.
     * Verifies handling of invalid user ID during logout.
     */
    @Test
    void testLogout_InvalidUserId() {
        when(authentication.getName()).thenReturn("invalid");

        assertThrows(NumberFormatException.class, () -> {
            authController.logout(authentication);
        });
    }

    /**
     * Test logout when service throws exception.
     * Verifies proper error handling during logout operation.
     */
    @Test
    void testLogout_ServiceException() {
        Long userId = 1L;
        when(authentication.getName()).thenReturn(userId.toString());
        doThrow(new RuntimeException("Logout failed")).when(authService).logout(userId);

        assertThrows(RuntimeException.class, () -> {
            authController.logout(authentication);
        });
        verify(authService, times(1)).logout(userId);
    }
}