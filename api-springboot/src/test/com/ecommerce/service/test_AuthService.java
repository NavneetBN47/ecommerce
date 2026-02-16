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
 * JUnit 5 test class for AuthService
 * Tests authentication operations including login and logout functionality
 * 
 * @author Test Generation Agent
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

    /**
     * Setup method to initialize test data before each test
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
     * Test successful login with username
     * Verifies that a valid user can login and receive a JWT token
     */
    @Test
    @DisplayName("Should successfully login with valid username and password")
    void testLogin_Success_WithUsername() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L))
                .thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationTime())
                .thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken("testuser", 1L);
    }

    /**
     * Test successful login with email
     * Verifies that a user can login using email instead of username
     */
    @Test
    @DisplayName("Should successfully login with valid email and password")
    void testLogin_Success_WithEmail() {
        // Arrange
        loginRequest.setIdentifier("test@example.com");
        when(userRepository.findByUsernameOrEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L))
                .thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationTime())
                .thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    /**
     * Test login failure with invalid username
     * Verifies that authentication fails when user is not found
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testLogin_Failure_UserNotFound() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login failure with inactive user
     * Verifies that deactivated users cannot login
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user is inactive")
    void testLogin_Failure_InactiveUser() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));

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
     * Test login failure with wrong password
     * Verifies that authentication fails with incorrect password
     */
    @Test
    @DisplayName("Should throw AuthenticationException when password is incorrect")
    void testLogin_Failure_WrongPassword() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    /**
     * Test successful logout
     * Verifies that logout clears user's cart
     */
    @Test
    @DisplayName("Should successfully logout and clear cart")
    void testLogout_Success() {
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
     * Verifies behavior when logout is called with null
     */
    @Test
    @DisplayName("Should handle logout with null userId")
    void testLogout_NullUserId() {
        // Arrange
        doNothing().when(cartService).clearCart(null);

        // Act
        authService.logout(null);

        // Assert
        verify(cartService).clearCart(null);
    }

    /**
     * Test login with null request
     * Verifies that null login request throws exception
     */
    @Test
    @DisplayName("Should throw exception when login request is null")
    void testLogin_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> authService.login(null));
    }

    /**
     * Test login response contains all user details
     * Verifies that UserDTO is properly mapped with all fields
     */
    @Test
    @DisplayName("Should return complete user details in login response")
    void testLogin_CompleteUserDetails() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L))
                .thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationTime())
                .thenReturn(3600000L);

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
     * Test login with empty password
     * Verifies that empty password is handled properly
     */
    @Test
    @DisplayName("Should handle login with empty password")
    void testLogin_EmptyPassword() {
        // Arrange
        loginRequest.setPassword("");
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "encodedPassword"))
                .thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    /**
     * Test login with empty identifier
     * Verifies that empty username/email is handled properly
     */
    @Test
    @DisplayName("Should handle login with empty identifier")
    void testLogin_EmptyIdentifier() {
        // Arrange
        loginRequest.setIdentifier("");
        when(userRepository.findByUsernameOrEmail(""))
                .thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username/email or password", exception.getMessage());
    }
}