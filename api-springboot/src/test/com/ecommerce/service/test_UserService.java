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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService
 * Tests user management operations including signup, login, profile retrieval, and updates
 * 
 * @author QA Automation Team
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
            .fullName("Updated User")
            .email("updated@example.com")
            .build();
    }

    /**
     * Test successful user signup
     * Verifies that a new user can register successfully
     */
    @Test
    @DisplayName("Should signup user successfully")
    void testSignup_Success() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).existsByUsername(signupRequest.getUsername());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test signup with duplicate username
     * Verifies that DuplicateResourceException is thrown
     */
    @Test
    @DisplayName("Should throw DuplicateResourceException when username already exists")
    void testSignup_DuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(signupRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername(signupRequest.getUsername());
        verify(userRepository, never()).save(any());
    }

    /**
     * Test successful user login
     * Verifies that a user can login with valid credentials
     */
    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // Act
        UserResponse response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    /**
     * Test login with non-existent user
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found during login")
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with invalid password
     * Verifies that InvalidCredentialsException is thrown
     */
    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    /**
     * Test getting user profile
     * Verifies that user profile is retrieved successfully
     */
    @Test
    @DisplayName("Should get user profile successfully")
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
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user profile not found")
    void testGetProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile(userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    /**
     * Test updating user profile
     * Verifies that user profile is updated successfully
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        UserResponse response = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(response);
        assertEquals(updateProfileRequest.getFullName(), testUser.getFullName());
        assertEquals(updateProfileRequest.getEmail(), testUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    /**
     * Test updating profile for non-existent user
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user profile")
    void testUpdateProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateProfile(userId, updateProfileRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
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
     * Test signup with empty username
     * Verifies behavior with empty username
     */
    @Test
    @DisplayName("Should handle signup with empty username")
    void testSignup_EmptyUsername() {
        // Arrange
        signupRequest.setUsername("");
        when(userRepository.existsByUsername("")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test login with empty password
     * Verifies behavior with empty password
     */
    @Test
    @DisplayName("Should handle login with empty password")
    void testLogin_EmptyPassword() {
        // Arrange
        loginRequest.setPassword("");
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    /**
     * Test update profile with null values
     * Verifies that null values are handled correctly
     */
    @Test
    @DisplayName("Should handle update profile with null values")
    void testUpdateProfile_NullValues() {
        // Arrange
        updateProfileRequest.setFullName(null);
        updateProfileRequest.setEmail(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        UserResponse response = userService.updateProfile(userId, updateProfileRequest);

        // Assert
        assertNotNull(response);
        assertNull(testUser.getFullName());
        assertNull(testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    /**
     * Test password encoding during signup
     * Verifies that password is properly encoded
     */
    @Test
    @DisplayName("Should encode password during signup")
    void testSignup_PasswordEncoding() {
        // Arrange
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPassword";
        signupRequest.setPassword(rawPassword);
        
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(encodedPassword, user.getPassword());
            return testUser;
        });

        // Act
        UserResponse response = userService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        verify(passwordEncoder).encode(rawPassword);
    }
}