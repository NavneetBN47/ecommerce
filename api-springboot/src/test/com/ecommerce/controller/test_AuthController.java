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
     */
    @Test
    void testRegister_NullDTO() {
        assertThrows(NullPointerException.class, () -> {
            authController.register(null);
        });
    }

    /**
     * Test registration when service throws exception
     */
    @Test
    void testRegister_ServiceException() {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
            .thenThrow(new RuntimeException("Registration failed"));

        assertThrows(RuntimeException.class, () -> {
            authController.register(registrationDTO);
        });
        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    /**
     * Test successful user login
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
     */
    @Test
    void testLogin_NullRequest() {
        assertThrows(NullPointerException.class, () -> {
            authController.login(null);
        });
    }

    /**
     * Test successful user logout
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
     * Test logout with invalid user ID
     */
    @Test
    void testLogout_InvalidUserId() {
        when(authentication.getName()).thenReturn("invalid");

        assertThrows(NumberFormatException.class, () -> {
            authController.logout(authentication);
        });
    }

    /**
     * Test logout when service throws exception
     */
    @Test
    void testLogout_ServiceException() {
        when(authentication.getName()).thenReturn("1");
        doThrow(new RuntimeException("Logout failed")).when(authService).logout(anyLong());

        assertThrows(RuntimeException.class, () -> {
            authController.logout(authentication);
        });
        verify(authService, times(1)).logout(1L);
    }

    /**
     * Test logout with null authentication
     */
    @Test
    void testLogout_NullAuthentication() {
        assertThrows(NullPointerException.class, () -> {
            authController.logout(null);
        });
    }
}