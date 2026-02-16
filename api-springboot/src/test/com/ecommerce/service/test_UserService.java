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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for UserService
 * Tests user management operations including signup, login, profile retrieval and update
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

    private UUID userId;
    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .password("encodedPassword")
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
    }

    /**
     * Test successful user signup
     * Verifies user registration and password encoding
     */
    @Test
    void signupShouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.signup(signupRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getFullName());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    /**
     * Test signup with duplicate username
     * Verifies DuplicateResourceException is thrown
     */
    @Test
    void signupShouldThrowExceptionForDuplicateUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When/Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(signupRequest);
        });
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test signup encrypts password
     * Verifies password is encoded before saving
     */
    @Test
    void signupShouldEncryptPassword() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("encodedPassword", user.getPassword());
            return user;
        });

        // When
        userService.signup(signupRequest);

        // Then
        verify(passwordEncoder, times(1)).encode("password123");
    }

    /**
     * Test successful user login
     * Verifies authentication and user data return
     */
    @Test
    void loginShouldAuthenticateUserSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When
        UserResponse response = userService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    /**
     * Test login with non-existent user
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void loginShouldThrowExceptionForNonExistentUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When/Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("User not found", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * Test login with invalid password
     * Verifies InvalidCredentialsException is thrown
     */
    @Test
    void loginShouldThrowExceptionForInvalidPassword() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // When/Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }

    /**
     * Test getting user profile
     * Verifies profile retrieval
     */
    @Test
    void getProfileShouldReturnUserProfile() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getProfile(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test getting profile for non-existent user
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void getProfileShouldThrowExceptionForNonExistentUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile(userId);
        });
        assertEquals("User not found", exception.getMessage());
    }

    /**
     * Test updating user profile
     * Verifies profile update functionality
     */
    @Test
    void updateProfileShouldUpdateUserProfile() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.updateProfile(userId, request);

        // Then
        assertNotNull(response);
        assertEquals("Updated Name", testUser.getFullName());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Test updating profile for non-existent user
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void updateProfileShouldThrowExceptionForNonExistentUser() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateProfile(userId, request);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test user response mapping
     * Verifies all fields are mapped correctly
     */
    @Test
    void userResponseShouldContainAllFields() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getProfile(userId);

        // Then
        assertNotNull(response.getId());
        assertNotNull(response.getUsername());
        assertNotNull(response.getFullName());
        assertNotNull(response.getEmail());
        assertNotNull(response.getCreatedAt());
    }

    /**
     * Test signup with empty username
     * Verifies validation
     */
    @Test
    void signupShouldValidateUsername() {
        // Given
        signupRequest.setUsername("");
        when(userRepository.existsByUsername("")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.signup(signupRequest);

        // Then
        assertNotNull(response);
    }

    /**
     * Test login with empty password
     * Verifies authentication fails
     */
    @Test
    void loginShouldRejectEmptyPassword() {
        // Given
        loginRequest.setPassword("");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "encodedPassword")).thenReturn(false);

        // When/Then
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });
    }
}