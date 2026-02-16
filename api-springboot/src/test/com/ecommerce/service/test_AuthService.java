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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for AuthService.
 * Tests authentication operations including login and logout functionality.
 * Uses Mockito for mocking dependencies.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
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

    /**
     * Set up test data before each test execution.
     */
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

        loginRequest = LoginRequestDTO.builder()
                .identifier("testuser")
                .password("password123")
                .build();
    }

    /**
     * Test successful login with valid credentials using username.
     * Verifies that JWT token is generated and user details are returned.
     */
    @Test
    @DisplayName("Should successfully login with valid username and password")
    void testLogin_WithValidUsername_ShouldReturnLoginResponse() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken("testuser", 1L);
        verify(jwtTokenProvider).getExpirationTime();
    }

    /**
     * Test successful login with valid credentials using email.
     * Verifies that JWT token is generated when email is used as identifier.
     */
    @Test
    @DisplayName("Should successfully login with valid email and password")
    void testLogin_WithValidEmail_ShouldReturnLoginResponse() {
        // Arrange
        loginRequest.setIdentifier("test@example.com");
        when(userRepository.findByUsernameOrEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("jwt-token-456");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    /**
     * Test login failure when user is not found.
     * Verifies that AuthenticationException is thrown with appropriate message.
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testLogin_WithNonExistentUser_ShouldThrowAuthenticationException() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());
        loginRequest.setIdentifier("nonexistent");

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login failure when user account is deactivated.
     * Verifies that AuthenticationException is thrown for inactive users.
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user account is deactivated")
    void testLogin_WithDeactivatedUser_ShouldThrowAuthenticationException() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
        assertEquals("User account is deactivated", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login failure with incorrect password.
     * Verifies that AuthenticationException is thrown when password doesn't match.
     */
    @Test
    @DisplayName("Should throw AuthenticationException with incorrect password")
    void testLogin_WithIncorrectPassword_ShouldThrowAuthenticationException() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    /**
     * Test successful logout operation.
     * Verifies that cart is cleared when user logs out.
     */
    @Test
    @DisplayName("Should successfully logout and clear cart")
    void testLogout_WithValidUserId_ShouldClearCart() {
        // Arrange
        Long userId = 1L;
        doNothing().when(cartService).clearCart(userId);

        // Act
        authService.logout(userId);

        // Assert
        verify(cartService).clearCart(userId);
    }

    /**
     * Test logout with null user ID.
     * Verifies behavior when null userId is provided.
     */
    @Test
    @DisplayName("Should handle logout with null userId")
    void testLogout_WithNullUserId_ShouldCallClearCart() {
        // Arrange
        doNothing().when(cartService).clearCart(null);

        // Act
        authService.logout(null);

        // Assert
        verify(cartService).clearCart(null);
    }

    /**
     * Test that user DTO mapping includes all required fields.
     * Verifies complete user information is returned in login response.
     */
    @Test
    @DisplayName("Should map all user fields correctly in login response")
    void testLogin_ShouldMapAllUserFieldsCorrectly() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        UserDTO userDTO = response.getUser();
        assertNotNull(userDTO);
        assertEquals(1L, userDTO.getId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("Test", userDTO.getFirstName());
        assertEquals("User", userDTO.getLastName());
        assertEquals("1234567890", userDTO.getPhoneNumber());
        assertTrue(userDTO.getActive());
    }

    /**
     * Test login with empty identifier.
     * Verifies handling of empty username/email.
     */
    @Test
    @DisplayName("Should throw AuthenticationException with empty identifier")
    void testLogin_WithEmptyIdentifier_ShouldThrowAuthenticationException() {
        // Arrange
        loginRequest.setIdentifier("");
        when(userRepository.findByUsernameOrEmail("")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    /**
     * Test login with null password.
     * Verifies handling of null password scenario.
     */
    @Test
    @DisplayName("Should throw AuthenticationException with null password")
    void testLogin_WithNullPassword_ShouldThrowAuthenticationException() {
        // Arrange
        loginRequest.setPassword(null);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(null, "encodedPassword")).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
        assertEquals("Invalid username/email or password", exception.getMessage());
    }
}