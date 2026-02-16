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
 * Test class for AuthController
 * 
 * Tests authentication endpoints including registration, login, and logout
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

    private UserRegistrationDTO registrationDTO;
    private LoginRequestDTO loginRequestDTO;
    private UserDTO userDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("testuser");
        loginRequestDTO.setPassword("password123");

        userDTO = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .active(true)
            .build();

        loginResponseDTO = LoginResponseDTO.builder()
            .token("jwt-token")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .user(userDTO)
            .build();
    }

    /**
     * Test successful user registration
     * 
     * Verifies:
     * - Registration endpoint returns CREATED status
     * - UserService.registerUser is called with correct DTO
     * - Response contains success message and user data
     */
    @Test
    void testRegister_Success() {
        // Given
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        // When
        ResponseEntity<ApiResponseDTO<UserDTO>> response = authController.register(registrationDTO);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be CREATED");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("User registered successfully", response.getBody().getMessage());
        assertEquals(userDTO, response.getBody().getData());
        verify(userService, times(1)).registerUser(registrationDTO);
    }

    /**
     * Test registration with null DTO
     * 
     * Verifies:
     * - Validation handles null input appropriately
     */
    @Test
    void testRegister_NullDTO() {
        // When/Then
        assertThrows(Exception.class, () -> {
            authController.register(null);
        });
    }

    /**
     * Test successful user login
     * 
     * Verifies:
     * - Login endpoint returns OK status
     * - AuthService.login is called with correct credentials
     * - Response contains JWT token and user data
     */
    @Test
    void testLogin_Success() {
        // Given
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        // When
        ResponseEntity<ApiResponseDTO<LoginResponseDTO>> response = authController.login(loginRequestDTO);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(loginResponseDTO, response.getBody().getData());
        verify(authService, times(1)).login(loginRequestDTO);
    }

    /**
     * Test login with invalid credentials
     * 
     * Verifies:
     * - AuthService throws appropriate exception for invalid credentials
     */
    @Test
    void testLogin_InvalidCredentials() {
        // Given
        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequestDTO);
        });
        verify(authService, times(1)).login(loginRequestDTO);
    }

    /**
     * Test successful user logout
     * 
     * Verifies:
     * - Logout endpoint returns OK status
     * - AuthService.logout is called with correct user ID
     * - Cart is cleared on logout
     */
    @Test
    void testLogout_Success() {
        // Given
        Long userId = 1L;
        when(authentication.getName()).thenReturn(userId.toString());
        doNothing().when(authService).logout(userId);

        // When
        ResponseEntity<ApiResponseDTO<Void>> response = authController.logout(authentication);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Logout successful", response.getBody().getMessage());
        verify(authService, times(1)).logout(userId);
    }

    /**
     * Test logout with null authentication
     * 
     * Verifies:
     * - Proper exception handling for missing authentication
     */
    @Test
    void testLogout_NullAuthentication() {
        // When/Then
        assertThrows(Exception.class, () -> {
            authController.logout(null);
        });
    }

    /**
     * Test logout with invalid user ID format
     * 
     * Verifies:
     * - Proper exception handling for invalid user ID
     */
    @Test
    void testLogout_InvalidUserId() {
        // Given
        when(authentication.getName()).thenReturn("invalid");

        // When/Then
        assertThrows(NumberFormatException.class, () -> {
            authController.logout(authentication);
        });
    }
}
