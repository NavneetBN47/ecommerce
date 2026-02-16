package com.ecommerce.service;

import com.ecommerce.dto.LoginRequestDTO;
import com.ecommerce.dto.LoginResponseDTO;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for AuthService
 * 
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

    private User user;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("$2a$10$encodedPassword")
            .firstName("Test")
            .lastName("User")
            .active(true)
            .build();

        loginRequest = new LoginRequestDTO();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("password123");
    }

    /**
     * Test successful login with username
     * 
     * Verifies:
     * - User is authenticated successfully
     * - JWT token is generated
     * - LoginResponseDTO contains user data and token
     */
    @Test
    void testLogin_SuccessWithUsername() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyLong())).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);

        // When
        LoginResponseDTO response = authService.login(loginRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.getIdentifier());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtTokenProvider, times(1)).generateToken(user.getUsername(), user.getId());
    }

    /**
     * Test successful login with email
     * 
     * Verifies:
     * - User can login with email instead of username
     * - JWT token is generated
     */
    @Test
    void testLogin_SuccessWithEmail() {
        // Given
        loginRequest.setIdentifier("test@example.com");
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyLong())).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600L);

        // When
        LoginResponseDTO response = authService.login(loginRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals("jwt-token", response.getToken());
        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.getIdentifier());
    }

    /**
     * Test login with invalid username/email
     * 
     * Verifies:
     * - AuthenticationException is thrown for non-existent user
     */
    @Test
    void testLogin_UserNotFound() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.empty());

        // When/Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.getIdentifier());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with incorrect password
     * 
     * Verifies:
     * - AuthenticationException is thrown for wrong password
     */
    @Test
    void testLogin_IncorrectPassword() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When/Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.getIdentifier());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    /**
     * Test login with deactivated user account
     * 
     * Verifies:
     * - AuthenticationException is thrown for inactive user
     */
    @Test
    void testLogin_DeactivatedAccount() {
        // Given
        user.setActive(false);
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));

        // When/Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("User account is deactivated", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.getIdentifier());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test successful logout
     * 
     * Verifies:
     * - User cart is cleared on logout
     * - CartService.clearCart is called with correct user ID
     */
    @Test
    void testLogout_Success() {
        // Given
        Long userId = 1L;
        doNothing().when(cartService).clearCart(anyLong());

        // When
        authService.logout(userId);

        // Then
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test logout with null user ID
     * 
     * Verifies:
     * - Appropriate exception handling for null user ID
     */
    @Test
    void testLogout_NullUserId() {
        // When/Then
        assertThrows(Exception.class, () -> {
            authService.logout(null);
        });
    }

    /**
     * Test logout when cart service throws exception
     * 
     * Verifies:
     * - Exception from cart service is propagated
     */
    @Test
    void testLogout_CartServiceException() {
        // Given
        Long userId = 1L;
        doThrow(new RuntimeException("Cart service error"))
            .when(cartService).clearCart(anyLong());

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            authService.logout(userId);
        });
        verify(cartService, times(1)).clearCart(userId);
    }
}
