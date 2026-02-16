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
 * Test class for UserService
 * Tests user management operations including signup, login, profile retrieval and updates
 * Uses Mockito for mocking repository and password encoder dependencies
 *
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
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

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .password("$2a$10$encodedPassword")
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
     * Verifies that a new user can be registered successfully
     */
    @Test
    @DisplayName("Should successfully register new user")
    void testSignup_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test signup with duplicate username
     * Verifies that DuplicateResourceException is thrown for existing username
     */
    @Test
    @DisplayName("Should throw DuplicateResourceException when username already exists")
    void testSignup_DuplicateUsername_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> userService.signup(signupRequest)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    /**
     * Test signup password encoding
     * Verifies that password is properly encoded during signup
     */
    @Test
    @DisplayName("Should encode password during signup")
    void testSignup_EncodesPassword() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("$2a$10$encodedPassword", user.getPassword());
            return testUser;
        });

        // Act
        userService.signup(signupRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test successful user login
     * Verifies that a user can login with valid credentials
     */
    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // Act
        UserResponse response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }

    /**
     * Test login with non-existent user
     * Verifies that ResourceNotFoundException is thrown for invalid username
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testLogin_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        loginRequest.setUsername("nonexistent");

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.login(loginRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with incorrect password
     * Verifies that InvalidCredentialsException is thrown for wrong password
     */
    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void testLogin_IncorrectPassword_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
    }

    /**
     * Test getting user profile
     * Verifies that user profile can be retrieved successfully
     */
    @Test
    @DisplayName("Should successfully get user profile")
    void testGetProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getProfile(userId);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).findById(userId);
    }

    /**
     * Test getting profile for non-existent user
     * Verifies that ResourceNotFoundException is thrown for invalid user ID
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile user not found")
    void testGetProfile_UserNotFound_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getProfile(nonExistentId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(nonExistentId);
    }

    /**
     * Test updating user profile
     * Verifies that user profile can be updated successfully
     */
    @Test
    @DisplayName("Should successfully update user profile")
    void testUpdateProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    /**
     * Test updating profile for non-existent user
     * Verifies that ResourceNotFoundException is thrown for invalid user ID
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void testUpdateProfile_UserNotFound_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updateProfile(nonExistentId, updateProfileRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any());
    }

    /**
     * Test UserResponse mapping
     * Verifies that User entity is correctly mapped to UserResponse DTO
     */
    @Test
    @DisplayName("Should correctly map User entity to UserResponse DTO")
    void testUserResponseMapping() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getProfile(userId);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getCreatedAt(), response.getCreatedAt());
    }

    /**
     * Test login with null username
     * Verifies behavior when login is attempted with null username
     */
    @Test
    @DisplayName("Should handle login with null username")
    void testLogin_NullUsername_ThrowsException() {
        // Arrange
        loginRequest.setUsername(null);
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.login(loginRequest)
        );

        verify(userRepository).findByUsername(null);
    }

    /**
     * Test signup with null password
     * Verifies that password encoder is called even with null password
     */
    @Test
    @DisplayName("Should handle signup with null password")
    void testSignup_NullPassword() {
        // Arrange
        signupRequest.setPassword(null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(null)).thenReturn("$2a$10$encodedNull");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.signup(signupRequest);

        // Assert
        verify(passwordEncoder).encode(null);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test update profile with partial data
     * Verifies that only provided fields are updated
     */
    @Test
    @DisplayName("Should update only provided profile fields")
    void testUpdateProfile_PartialUpdate() {
        // Arrange
        String originalEmail = testUser.getEmail();
        updateProfileRequest.setEmail(null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Name", testUser.getFullName());
        assertNull(testUser.getEmail());
        verify(userRepository).save(testUser);
    }
}