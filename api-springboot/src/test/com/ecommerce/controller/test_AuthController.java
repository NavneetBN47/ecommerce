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

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("Password123!");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("testuser");
        loginRequestDTO.setPassword("Password123!");

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
     * Verifies that a new user can be registered and returns HTTP 201 CREATED status.
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
     * Test registration with null DTO.
     * Verifies that appropriate exception handling occurs.
     */
    @Test
    void testRegister_NullDTO() {
        assertThrows(Exception.class, () -> {
            authController.register(null);
        });
    }

    /**
     * Test successful user login.
     * Verifies that valid credentials return a JWT token and HTTP 200 OK status.
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
        assertEquals("jwt-token-123", response.getBody().getData().getToken());
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test login with invalid credentials.
     * Verifies that authentication service is called even with invalid data.
     */
    @Test
    void testLogin_InvalidCredentials() {
        LoginRequestDTO invalidRequest = new LoginRequestDTO();
        invalidRequest.setIdentifier("wronguser");
        invalidRequest.setPassword("wrongpass");

        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(invalidRequest);
        });
        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    /**
     * Test successful user logout.
     * Verifies that cart is cleaned up and HTTP 200 OK status is returned.
     */
    @Test
    void testLogout_Success() {
        when(authentication.getName()).thenReturn("1");
        doNothing().when(authService).logout(anyLong());

        ResponseEntity<ApiResponseDTO<Void>> response = authController.logout(authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logout successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(authService, times(1)).logout(1L);
    }

    /**
     * Test logout with invalid user ID.
     * Verifies that service layer handles invalid user scenarios.
     */
    @Test
    void testLogout_InvalidUserId() {
        when(authentication.getName()).thenReturn("invalid");

        assertThrows(NumberFormatException.class, () -> {
            authController.logout(authentication);
        });
    }

    /**
     * Test logout when authentication is null.
     * Verifies proper null handling.
     */
    @Test
    void testLogout_NullAuthentication() {
        assertThrows(NullPointerException.class, () -> {
            authController.logout(null);
        });
    }
}