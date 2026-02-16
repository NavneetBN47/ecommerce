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
 * Uses Mockito for mocking dependencies
 *
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
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
    void testLogin_Success_WithUsername() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword()))
            .thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L))
            .thenReturn("jwt.token.here");
        when(jwtTokenProvider.getExpirationTime())
            .thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
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
        when(passwordEncoder.matches("password123", testUser.getPassword()))
            .thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L))
            .thenReturn("jwt.token.here");
        when(jwtTokenProvider.getExpirationTime())
            .thenReturn(3600000L);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    /**
     * Test login failure with invalid username/email
     * Verifies that authentication exception is thrown for non-existent user
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testLogin_Failure_UserNotFound() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("nonexistent"))
            .thenReturn(Optional.empty());
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
     * Test login failure with incorrect password
     * Verifies that authentication exception is thrown for wrong password
     */
    @Test
    @DisplayName("Should throw AuthenticationException when password is incorrect")
    void testLogin_Failure_IncorrectPassword() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword()))
            .thenReturn(false);
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    /**
     * Test login failure with deactivated account
     * Verifies that authentication exception is thrown for inactive users
     */
    @Test
    @DisplayName("Should throw AuthenticationException when user account is deactivated")
    void testLogin_Failure_DeactivatedAccount() {
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
     * Test successful logout
     * Verifies that user cart is cleared during logout
     */
    @Test
    @DisplayName("Should successfully logout user and clear cart")
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
     * Test logout with null user ID
     * Verifies behavior when logout is called with null user ID
     */
    @Test
    @DisplayName("Should handle logout with null user ID")
    void testLogout_WithNullUserId() {
        // Arrange
        doNothing().when(cartService).clearCart(null);

        // Act
        authService.logout(null);

        // Assert
        verify(cartService).clearCart(null);
    }

    /**
     * Test login with null identifier
     * Verifies that appropriate exception is thrown for null identifier
     */
    @Test
    @DisplayName("Should throw exception when login identifier is null")
    void testLogin_Failure_NullIdentifier() {
        // Arrange
        loginRequest.setIdentifier(null);
        when(userRepository.findByUsernameOrEmail(null))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            AuthenticationException.class,
            () -> authService.login(loginRequest)
        );

        verify(userRepository).findByUsernameOrEmail(null);
    }

    /**
     * Test login with empty password
     * Verifies that authentication fails with empty password
     */
    @Test
    @DisplayName("Should fail login when password is empty")
    void testLogin_Failure_EmptyPassword() {
        // Arrange
        loginRequest.setPassword("");
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", testUser.getPassword()))
            .thenReturn(false);

        // Act & Assert
        assertThrows(
            AuthenticationException.class,
            () -> authService.login(loginRequest)
        );

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("", testUser.getPassword());
    }

    /**
     * Test UserDTO mapping
     * Verifies that User entity is correctly mapped to UserDTO
     */
    @Test
    @DisplayName("Should correctly map User entity to UserDTO")
    void testUserDTOMapping() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword()))
            .thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L))
            .thenReturn("jwt.token.here");
        when(jwtTokenProvider.getExpirationTime())
            .thenReturn(3600000L);

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
}