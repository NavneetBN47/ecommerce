package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService
 * 
 * Tests user operations including signup, login, profile retrieval and update
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_UserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
            .id(userId)
            .username("testuser")
            .password("$2a$10$encodedPassword")
            .fullName("Test User")
            .email("test@example.com")
            .build();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");
        signupRequest.setEmail("test@example.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setFullName("Updated Name");
        updateProfileRequest.setEmail("updated@example.com");
    }

    /**
     * Test successful user signup
     * 
     * Verifies:
     * - User is registered successfully
     * - Password is encoded
     * - UserResponse is returned with correct data
     */
    @Test
    void testSignup_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.signup(signupRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository, times(1)).existsByUsername(signupRequest.getUsername());
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test signup with duplicate username
     * 
     * Verifies:
     * - DuplicateResourceException is thrown for existing username
     */
    @Test
    void testSignup_DuplicateUsername() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When/Then
        assertThrows(Exception.class, () -> {
            userService.signup(signupRequest);
        });
        verify(userRepository, times(1)).existsByUsername(signupRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test successful user login
     * 
     * Verifies:
     * - User is authenticated successfully
     * - Password is verified
     * - UserResponse is returned with correct data
     */
    @Test
    void testLogin_Success() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When
        UserResponse response = userService.login(loginRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
    }

    /**
     * Test login with non-existent user
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent user
     */
    @Test
    void testLogin_UserNotFound() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            userService.login(loginRequest);
        });
        verify(userRepository, times(1)).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with incorrect password
     * 
     * Verifies:
     * - InvalidCredentialsException is thrown for wrong password
     */
    @Test
    void testLogin_IncorrectPassword() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When/Then
        assertThrows(Exception.class, () -> {
            userService.login(loginRequest);
        });
        verify(userRepository, times(1)).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
    }

    /**
     * Test getting user profile successfully
     * 
     * Verifies:
     * - User profile is retrieved
     * - UserResponse is returned with correct data
     */
    @Test
    void testGetProfile_Success() {
        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getProfile(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test getting profile for non-existent user
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent user
     */
    @Test
    void testGetProfile_UserNotFound() {
        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            userService.getProfile(userId);
        });
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test updating user profile successfully
     * 
     * Verifies:
     * - User profile is updated
     * - Updated user is saved
     * - UserResponse is returned with updated data
     */
    @Test
    void testUpdateProfile_Success() {
        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.updateProfile(userId, updateProfileRequest);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals("Updated Name", user.getFullName());
        assertEquals("updated@example.com", user.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    /**
     * Test updating profile for non-existent user
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent user
     */
    @Test
    void testUpdateProfile_UserNotFound() {
        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            userService.updateProfile(userId, updateProfileRequest);
        });
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
