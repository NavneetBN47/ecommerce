package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test class for UserService.
 * Tests all public methods with proper mocking of UserRepository and JwtService dependencies.
 * Covers normal execution paths, edge cases, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class test_UserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setPassword("password123");
        registrationRequest.setFullName("Test User");
        registrationRequest.setEmail("test@example.com");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    /**
     * Test successful user registration.
     * Verifies that the service creates and saves user correctly.
     */
    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        // Given
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.registerUser(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test user registration with minimum required fields.
     * Verifies that registration works without optional email field.
     */
    @Test
    @DisplayName("Should register user without email")
    void testRegisterUser_WithoutEmail() {
        // Given
        registrationRequest.setEmail(null);
        testUser.setEmail(null);
        
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.registerUser(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNull(result.getEmail());
        
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test user registration with duplicate username.
     * Verifies that the service throws DuplicateResourceException.
     */
    @Test
    @DisplayName("Should throw exception for duplicate username")
    void testRegisterUser_DuplicateUsername() {
        // Given
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        
        assertEquals("Username already exists: testuser", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test user registration with duplicate email.
     * Verifies that the service throws DuplicateResourceException.
     */
    @Test
    @DisplayName("Should throw exception for duplicate email")
    void testRegisterUser_DuplicateEmail() {
        // Given
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        
        assertEquals("Email already exists: test@example.com", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test successful user login.
     * Verifies that the service authenticates user and generates JWT token.
     */
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() {
        // Given
        String expectedToken = "jwt-token-123";
        
        when(userRepository.findByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser.getUserId(), testUser.getUsername()))
            .thenReturn(expectedToken);

        // When
        LoginResponse result = userService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        
        verify(userRepository, times(1)).findByUsernameAndPassword("testuser", "password123");
        verify(jwtService, times(1)).generateToken(1L, "testuser");
    }

    /**
     * Test login with invalid credentials.
     * Verifies that the service throws InvalidCredentialsException.
     */
    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void testLogin_InvalidCredentials() {
        // Given
        when(userRepository.findByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword()))
            .thenReturn(Optional.empty());

        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndPassword("testuser", "password123");
        verify(jwtService, never()).generateToken(any(), any());
    }

    /**
     * Test login with wrong password.
     * Verifies that the service handles wrong password correctly.
     */
    @Test
    @DisplayName("Should handle wrong password")
    void testLogin_WrongPassword() {
        // Given
        loginRequest.setPassword("wrongpassword");
        
        when(userRepository.findByUsernameAndPassword(loginRequest.getUsername(), "wrongpassword"))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndPassword("testuser", "wrongpassword");
    }

    /**
     * Test login with non-existent username.
     * Verifies that the service handles non-existent username correctly.
     */
    @Test
    @DisplayName("Should handle non-existent username")
    void testLogin_NonExistentUsername() {
        // Given
        loginRequest.setUsername("nonexistent");
        
        when(userRepository.findByUsernameAndPassword("nonexistent", "password123"))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });
        
        verify(userRepository, times(1)).findByUsernameAndPassword("nonexistent", "password123");
    }

    /**
     * Test successful user profile retrieval.
     * Verifies that the service returns correct user profile.
     */
    @Test
    @DisplayName("Should get user profile successfully")
    void testGetUserProfile_Success() {
        // Given
        Long userId = 1L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserProfile(userId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test user profile retrieval for non-existent user.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception for non-existent user profile")
    void testGetUserProfile_UserNotFound() {
        // Given
        Long userId = 999L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfile(userId);
        });
        
        assertEquals("User not found: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test successful user profile update.
     * Verifies that the service updates user profile correctly.
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateUserProfile_Success() {
        // Given
        Long userId = 1L;
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFullName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        
        User updatedUser = new User();
        updatedUser.setUserId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setCreatedAt(testUser.getCreatedAt());
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserProfile(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@example.com", result.getEmail());
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test user profile update with partial data.
     * Verifies that the service updates only provided fields.
     */
    @Test
    @DisplayName("Should update user profile with partial data")
    void testUpdateUserProfile_PartialUpdate() {
        // Given
        Long userId = 1L;
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFullName("Partially Updated Name");
        // email is null - should not be updated
        
        User updatedUser = new User();
        updatedUser.setUserId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setFullName("Partially Updated Name");
        updatedUser.setEmail("test@example.com"); // Original email preserved
        updatedUser.setCreatedAt(testUser.getCreatedAt());
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserProfile(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Partially Updated Name", result.getFullName());
        assertEquals("test@example.com", result.getEmail()); // Original email preserved
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test user profile update with null values.
     * Verifies that null values don't overwrite existing data.
     */
    @Test
    @DisplayName("Should not update fields with null values")
    void testUpdateUserProfile_NullValues() {
        // Given
        Long userId = 1L;
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFullName(null);
        updateRequest.setEmail(null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse result = userService.updateUserProfile(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test User", result.getFullName()); // Original value preserved
        assertEquals("test@example.com", result.getEmail()); // Original value preserved
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test user profile update for non-existent user.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void testUpdateUserProfile_UserNotFound() {
        // Given
        Long userId = 999L;
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFullName("Updated Name");
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserProfile(userId, updateRequest);
        });
        
        assertEquals("User not found: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test user mapping with all fields.
     * Verifies that the service correctly maps all user fields to response.
     */
    @Test
    @DisplayName("Should map all user fields correctly")
    void testUserMapping_AllFields() {
        // Given
        Long userId = 1L;
        testUser.setFullName("Very Long Full Name With Multiple Words");
        testUser.setEmail("very.long.email.address@example.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserProfile(userId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Very Long Full Name With Multiple Words", result.getFullName());
        assertEquals("very.long.email.address@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
    }
}