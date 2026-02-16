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
 * Unit test class for UserService.
 * Tests user management operations including signup, login, and profile management.
 * Uses Mockito for mocking repository and encoder dependencies.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
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
     * Set up test data before each test execution.
     */
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
                .email("new@example.com")
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
     * Test successful user signup.
     * Verifies that user is created with encoded password.
     */
    @Test
    @DisplayName("Should signup user successfully")
    void testSignup_WithValidRequest_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test signup with duplicate username.
     * Verifies that DuplicateResourceException is thrown.
     */
    @Test
    @DisplayName("Should throw DuplicateResourceException when username exists")
    void testSignup_WithDuplicateUsername_ShouldThrowDuplicateResourceException() {
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
     * Test that password is encoded during signup.
     * Verifies that plain text password is not stored.
     */
    @Test
    @DisplayName("Should encode password during signup")
    void testSignup_ShouldEncodePassword() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("encodedPassword123", user.getPassword());
            return testUser;
        });

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        verify(passwordEncoder).encode("password123");
    }

    /**
     * Test successful user login.
     * Verifies that user can login with correct credentials.
     */
    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void testLogin_WithValidCredentials_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Act
        UserResponse response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    /**
     * Test login with non-existent username.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found during login")
    void testLogin_WithNonExistentUser_ShouldThrowResourceNotFoundException() {
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
     * Test login with incorrect password.
     * Verifies that InvalidCredentialsException is thrown.
     */
    @Test
    @DisplayName("Should throw InvalidCredentialsException with wrong password")
    void testLogin_WithIncorrectPassword_ShouldThrowInvalidCredentialsException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(loginRequest)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    /**
     * Test getting user profile successfully.
     * Verifies that user profile is retrieved correctly.
     */
    @Test
    @DisplayName("Should get user profile successfully")
    void testGetProfile_WithValidUserId_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getProfile(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFullName(), response.getFullName());
        verify(userRepository).findById(userId);
    }

    /**
     * Test getting profile for non-existent user.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting non-existent profile")
    void testGetProfile_WithInvalidUserId_ShouldThrowResourceNotFoundException() {
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
     * Test updating user profile successfully.
     * Verifies that profile fields are updated correctly.
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_WithValidRequest_ShouldUpdateProfile() {
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
     * Test updating profile for non-existent user.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent profile")
    void testUpdateProfile_WithInvalidUserId_ShouldThrowResourceNotFoundException() {
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
     * Test that user response mapping includes all required fields.
     * Verifies complete user information is returned.
     */
    @Test
    @DisplayName("Should map all user fields correctly in response")
    void testGetProfile_ShouldMapAllFieldsCorrectly() {
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
     * Test signup with all required fields.
     * Verifies that all user fields are set during signup.
     */
    @Test
    @DisplayName("Should set all user fields during signup")
    void testSignup_ShouldSetAllUserFields() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("newuser", user.getUsername());
            assertEquals("encodedPassword", user.getPassword());
            assertEquals("New User", user.getFullName());
            assertEquals("new@example.com", user.getEmail());
            return testUser;
        });

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test that username is not changed during profile update.
     * Verifies that only allowed fields are updated.
     */
    @Test
    @DisplayName("Should not change username during profile update")
    void testUpdateProfile_ShouldNotChangeUsername() {
        // Arrange
        String originalUsername = testUser.getUsername();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(response);
        assertEquals(originalUsername, testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    /**
     * Test login with empty password.
     * Verifies proper handling of empty password.
     */
    @Test
    @DisplayName("Should handle empty password during login")
    void testLogin_WithEmptyPassword_ShouldThrowInvalidCredentialsException() {
        // Arrange
        loginRequest.setPassword("");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "encodedPassword")).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(loginRequest)
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }
}