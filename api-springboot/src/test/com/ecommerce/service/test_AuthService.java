package com.ecommerce.service;

import com.ecommerce.dto.LoginRequestDTO;
import com.ecommerce.dto.LoginResponseDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.AuthenticationException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for AuthService
 * Tests authentication operations including login and logout
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_AuthService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CartService cartService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("encodedPassword")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("1234567890")
            .active(true)
            .build();

        loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("password123");
    }

    /**
     * Test successful login with username
     * Verifies JWT token generation and user data return
     */
    @Test
    void loginShouldReturnLoginResponseWithToken() {
        // Given
        String jwtToken = "jwt.token.here";
        Long expiresIn = 3600L;

        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn(jwtToken);
        when(jwtTokenProvider.getExpirationTime()).thenReturn(expiresIn);

        // When
        LoginResponseDTO response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(expiresIn, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        verify(userRepository, times(1)).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    /**
     * Test successful login with email
     * Verifies email-based authentication
     */
    @Test
    void loginShouldAcceptEmailAsIdentifier() {
        // Given
        loginRequest.setIdentifier("test@example.com");
        String jwtToken = "jwt.token.here";

        when(userRepository.findByUsernameOrEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), any(Long.class))).thenReturn(jwtToken);
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);

        // When
        LoginResponseDTO response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        verify(userRepository, times(1)).findByUsernameOrEmail("test@example.com");
    }

    /**
     * Test login with invalid username
     * Verifies AuthenticationException is thrown
     */
    @Test
    void loginShouldThrowExceptionForInvalidUsername() {
        // Given
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.empty());

        // When/Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with invalid password
     * Verifies AuthenticationException is thrown
     */
    @Test
    void loginShouldThrowExceptionForInvalidPassword() {
        // Given
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // When/Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    /**
     * Test login with inactive user account
     * Verifies AuthenticationException is thrown
     */
    @Test
    void loginShouldThrowExceptionForInactiveUser() {
        // Given
        testUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // When/Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("User account is deactivated", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test logout clears user cart
     * Verifies cart service is called
     */
    @Test
    void logoutShouldClearUserCart() {
        // Given
        Long userId = 1L;
        doNothing().when(cartService).clearCart(userId);

        // When
        authService.logout(userId);

        // Then
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with null user ID
     * Verifies proper handling
     */
    @Test
    void logoutShouldHandleNullUserId() {
        // Given
        doThrow(new IllegalArgumentException("User ID cannot be null"))
            .when(cartService).clearCart(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.logout(null);
        });
    }

    /**
     * Test login response contains complete user data
     * Verifies all user fields are mapped correctly
     */
    @Test
    void loginShouldReturnCompleteUserData() {
        // Given
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), any(Long.class))).thenReturn("token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);

        // When
        LoginResponseDTO response = authService.login(loginRequest);

        // Then
        UserDTO user = response.getUser();
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("1234567890", user.getPhoneNumber());
        assertTrue(user.getActive());
    }

    /**
     * Test login with empty password
     * Verifies validation
     */
    @Test
    void loginShouldHandleEmptyPassword() {
        // Given
        loginRequest.setPassword("");
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "encodedPassword")).thenReturn(false);

        // When/Then
        assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
    }

    /**
     * Test logout multiple times for same user
     * Verifies idempotency
     */
    @Test
    void logoutShouldBeIdempotent() {
        // Given
        Long userId = 1L;
        doNothing().when(cartService).clearCart(userId);

        // When
        authService.logout(userId);
        authService.logout(userId);

        // Then
        verify(cartService, times(2)).clearCart(userId);
    }
}