package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService
 * Tests user management operations including signup, login, profile retrieval and updates
 */
@ExtendWith(MockitoExtension.class)
class test_UserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .password("encodedPassword")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

        signupRequest = SignupRequest.builder()
            .username("newuser")
            .password("password123")
            .fullName("New User")
            .email("newuser@example.com")
            .build();

        loginRequest = LoginRequest.builder()
            .username("testuser")
            .password("password123")
            .build();

        updateProfileRequest = UpdateProfileRequest.builder()
            .fullName("Updated Name")
            .email("updated@example.com")
            .build();
    }

    /**
     * Test user signup with valid data
     */
    @Test
    void testSignup_ValidData_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.signup(signupRequest);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test signup with existing username
     */
    @Test
    void testSignup_ExistingUsername_ThrowsException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(signupRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any());
    }

    /**
     * Test signup encrypts password
     */
    @Test
    void testSignup_EncryptsPassword() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("encodedPassword", user.getPassword());
            return testUser;
        });

        userService.signup(signupRequest);

        verify(passwordEncoder).encode("password123");
    }

    /**
     * Test successful login
     */
    @Test
    void testLogin_ValidCredentials_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        UserResponse result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    /**
     * Test login with non-existent user
     */
    @Test
    void testLogin_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with incorrect password
     */
    @Test
    void testLogin_IncorrectPassword_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    /**
     * Test getting user profile successfully
     */
    @Test
    void testGetProfile_ValidUserId_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getProfile(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findById(userId);
    }

    /**
     * Test getting profile for non-existent user
     */
    @Test
    void testGetProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile(userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    /**
     * Test updating user profile successfully
     */
    @Test
    void testUpdateProfile_ValidData_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateProfile(userId, updateProfileRequest);

        assertNotNull(result);
        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    /**
     * Test updating profile for non-existent user
     */
    @Test
    void testUpdateProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateProfile(userId, updateProfileRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    /**
     * Test user response mapping includes all fields
     */
    @Test
    void testGetProfile_MapsAllFields_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getProfile(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
    }

    /**
     * Test signup creates user with all provided fields
     */
    @Test
    void testSignup_CreatesUserWithAllFields() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("newuser", user.getUsername());
            assertEquals("encodedPassword", user.getPassword());
            assertEquals("New User", user.getFullName());
            assertEquals("newuser@example.com", user.getEmail());
            return testUser;
        });

        userService.signup(signupRequest);

        verify(userRepository).save(any(User.class));
    }

    /**
     * Test update profile only updates allowed fields
     */
    @Test
    void testUpdateProfile_OnlyUpdatesAllowedFields() {
        String originalUsername = testUser.getUsername();
        String originalPassword = testUser.getPassword();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateProfile(userId, updateProfileRequest);

        assertEquals(originalUsername, testUser.getUsername());
        assertEquals(originalPassword, testUser.getPassword());
        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("updated@example.com", testUser.getEmail());
    }

    /**
     * Test login with null username
     */
    @Test
    void testLogin_NullUsername_ThrowsException() {
        loginRequest.setUsername(null);
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
    }

    /**
     * Test signup with null password gets encoded
     */
    @Test
    void testSignup_HandlesNullPassword() {
        signupRequest.setPassword(null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(null)).thenReturn("encodedNull");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.signup(signupRequest);

        verify(passwordEncoder).encode(null);
    }
}