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
 * Test class for AuthService
 * Tests authentication operations including login and logout functionality
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Test Suite")
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
            .password("$2a$10$encodedPassword")
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
     * Test successful login with username
     * Verifies that a valid user can login and receive a JWT token
     */
    @Test
    @DisplayName("Should successfully login with valid username and password")
    void testLoginSuccess_WithUsername() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser.getUsername(), testUser.getId())).thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(testUser.getUsername(), response.getUser().getUsername());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
        verify(jwtTokenProvider).generateToken(testUser.getUsername(), testUser.getId());
    }

    /**
     * Test successful login with email
     * Verifies that a user can login using email instead of username
     */
    @Test
    @DisplayName("Should successfully login with valid email and password")
    void testLoginSuccess_WithEmail() {
        // Arrange
        loginRequest.setIdentifier("test@example.com");
        when(userRepository.findByUsernameOrEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser.getUsername(), testUser.getId())).thenReturn("jwt-token-456");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    /**
     * Test login failure with invalid username
     * Verifies that authentication fails when user is not found
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testLoginFailure_UserNotFound() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());
        loginRequest.setIdentifier("nonexistent");

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login failure with incorrect password
     * Verifies that authentication fails when password doesn't match
     */
    @Test
    @DisplayName("Should throw AuthenticationException when password is incorrect")
    void testLoginFailure_IncorrectPassword() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    /**
     * Test login failure with inactive user account
     * Verifies that deactivated users cannot login
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user account is deactivated")
    void testLoginFailure_InactiveUser() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User account is deactivated", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test successful logout
     * Verifies that logout clears the user's cart
     */
    @Test
    @DisplayName("Should successfully logout and clear cart")
    void testLogoutSuccess() {
        // Arrange
        Long userId = 1L;
        doNothing().when(cartService).clearCart(userId);

        // Act
        authService.logout(userId);

        // Assert
        verify(cartService).clearCart(userId);
    }

    /**
     * Test logout with null userId
     * Verifies behavior when logout is called with null user ID
     */
    @Test
    @DisplayName("Should handle logout with null userId")
    void testLogout_NullUserId() {
        // Arrange
        Long userId = null;
        doNothing().when(cartService).clearCart(userId);

        // Act
        authService.logout(userId);

        // Assert
        verify(cartService).clearCart(userId);
    }

    /**
     * Test UserDTO mapping
     * Verifies that User entity is correctly mapped to UserDTO
     */
    @Test
    @DisplayName("Should correctly map User entity to UserDTO")
    void testUserDTOMapping() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser.getUsername(), testUser.getId())).thenReturn("token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);
        UserDTO userDTO = response.getUser();

        // Assert
        assertNotNull(userDTO);
        assertEquals(testUser.getId(), userDTO.getId());
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getEmail(), userDTO.getEmail());
        assertEquals(testUser.getFirstName(), userDTO.getFirstName());
        assertEquals(testUser.getLastName(), userDTO.getLastName());
        assertEquals(testUser.getPhoneNumber(), userDTO.getPhoneNumber());
        assertEquals(testUser.getActive(), userDTO.getActive());
    }

    /**
     * Test login with empty identifier
     * Verifies handling of empty username/email
     */
    @Test
    @DisplayName("Should throw AuthenticationException when identifier is empty")
    void testLoginFailure_EmptyIdentifier() {
        // Arrange
        loginRequest.setIdentifier("");
        when(userRepository.findByUsernameOrEmail("")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    /**
     * Test login with null password
     * Verifies handling of null password input
     */
    @Test
    @DisplayName("Should throw AuthenticationException when password is null")
    void testLoginFailure_NullPassword() {
        // Arrange
        loginRequest.setPassword(null);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(null, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
    }
}