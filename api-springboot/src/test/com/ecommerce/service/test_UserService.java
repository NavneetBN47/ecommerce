package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
 * JUnit 5 test class for UserService
 * Tests user management operations including signup, login, and profile management
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Test Suite")
class test_UserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UpdateProfileRequest updateProfileRequest;
    private UUID userId;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .password("encodedPassword123")
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
     * Test successful user signup
     * Verifies that new user is registered successfully
     */
    @Test
    @DisplayName("Should successfully register new user")
    void testSignup_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.signup(signupRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test signup with duplicate username
     * Verifies that exception is thrown when username already exists
     */
    @Test
    @DisplayName("Should throw DuplicateResourceException when username already exists")
    void testSignup_DuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.signup(signupRequest)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test signup with password encoding
     * Verifies that password is properly encoded before saving
     */
    @Test
    @DisplayName("Should encode password before saving user")
    void testSignup_PasswordEncoding() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("encodedPassword123", user.getPassword());
            return user;
        });

        // Act
        userService.signup(signupRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
    }

    /**
     * Test successful user login
     * Verifies that user can login with valid credentials
     */
    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);

        // Act
        UserResponse result = userService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword123");
    }

    /**
     * Test login with non-existent user
     * Verifies that exception is thrown when user not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.login(loginRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with invalid password
     * Verifies that exception is thrown when password doesn't match
     */
    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(passwordEncoder).matches("password123", "encodedPassword123");
    }

    /**
     * Test getting user profile successfully
     * Verifies that user profile is retrieved
     */
    @Test
    @DisplayName("Should successfully retrieve user profile")
    void testGetProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getProfile(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        verify(userRepository).findById(userId);
    }

    /**
     * Test getting profile for non-existent user
     * Verifies that exception is thrown when user not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting non-existent profile")
    void testGetProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfile(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    /**
     * Test updating user profile successfully
     * Verifies that user profile is updated with new information
     */
    @Test
    @DisplayName("Should successfully update user profile")
    void testUpdateProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    /**
     * Test updating profile for non-existent user
     * Verifies that exception is thrown when user not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void testUpdateProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateProfile(userId, updateProfileRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test user response mapping
     * Verifies that all user fields are correctly mapped to response DTO
     */
    @Test
    @DisplayName("Should correctly map User entity to UserResponse DTO")
    void testLogin_CorrectMapping() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);

        // Act
        UserResponse result = userService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getFullName(), result.getFullName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
    }

    /**
     * Test signup with null request
     * Verifies that null request is handled properly
     */
    @Test
    @DisplayName("Should handle null signup request")
    void testSignup_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.signup(null));
    }

    /**
     * Test login with null request
     * Verifies that null request is handled properly
     */
    @Test
    @DisplayName("Should handle null login request")
    void testLogin_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.login(null));
    }

    /**
     * Test update profile with partial data
     * Verifies that only provided fields are updated
     */
    @Test
    @DisplayName("Should update only provided profile fields")
    void testUpdateProfile_PartialUpdate() {
        // Arrange
        String originalUsername = testUser.getUsername();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(result);
        assertEquals(originalUsername, testUser.getUsername()); // Username should not change
        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("updated@example.com", testUser.getEmail());
    }

    /**
     * Test signup creates user with correct fields
     * Verifies that all signup fields are properly set
     */
    @Test
    @DisplayName("Should create user with all signup fields")
    void testSignup_AllFieldsSet() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("newuser", user.getUsername());
            assertEquals("encodedPassword123", user.getPassword());
            assertEquals("New User", user.getFullName());
            assertEquals("newuser@example.com", user.getEmail());
            return user;
        });

        // Act
        userService.signup(signupRequest);

        // Assert
        verify(userRepository).save(any(User.class));
    }
}