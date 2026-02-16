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
 * Test class for AuthService
 * Tests authentication operations including login and logout functionality
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

        loginRequest = LoginRequestDTO.builder()
            .identifier("testuser")
            .password("password123")
            .build();
    }

    /**
     * Test successful login with username
     */
    @Test
    void testLogin_WithUsername_Success() {
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
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
     */
    @Test
    void testLogin_WithEmail_Success() {
        loginRequest.setIdentifier("test@example.com");
        when(userRepository.findByUsernameOrEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    /**
     * Test login with invalid username or email
     */
    @Test
    void testLogin_UserNotFound_ThrowsException() {
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with inactive user account
     */
    @Test
    void testLogin_InactiveUser_ThrowsException() {
        testUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User account is deactivated", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with incorrect password
     */
    @Test
    void testLogin_IncorrectPassword_ThrowsException() {
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyString(), any());
    }

    /**
     * Test successful logout
     */
    @Test
    void testLogout_Success() {
        Long userId = 1L;
        doNothing().when(cartService).clearCart(userId);

        authService.logout(userId);

        verify(cartService).clearCart(userId);
    }

    /**
     * Test logout with null userId
     */
    @Test
    void testLogout_WithNullUserId() {
        doNothing().when(cartService).clearCart(null);

        authService.logout(null);

        verify(cartService).clearCart(null);
    }

    /**
     * Test login response contains all user details
     */
    @Test
    void testLogin_ResponseContainsAllUserDetails() {
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        LoginResponseDTO response = authService.login(loginRequest);

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
     */
    @Test
    void testLogin_EmptyPassword_ThrowsException() {
        loginRequest.setPassword("");
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "encodedPassword")).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    /**
     * Test login with null identifier
     */
    @Test
    void testLogin_NullIdentifier_ThrowsException() {
        loginRequest.setIdentifier(null);
        when(userRepository.findByUsernameOrEmail(null)).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username/email or password", exception.getMessage());
    }
}