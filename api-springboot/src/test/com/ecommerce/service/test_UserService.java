package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService
 * 
 * Tests user management operations including:
 * - User registration (signup)
 * - User authentication (login)
 * - Profile retrieval and updates
 * - Validation and error handling
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class test_UserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;
    private UserSignupRequest signupRequest;
    private UserLoginRequest loginRequest;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = User.builder()
            .id(testUserId)
            .username("testuser")
            .password("encodedPassword")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(false)
            .build();

        signupRequest = UserSignupRequest.builder()
            .username("newuser")
            .password("password123")
            .fullName("New User")
            .email("newuser@example.com")
            .build();

        loginRequest = UserLoginRequest.builder()
            .username("testuser")
            .password("password123")
            .build();
    }

    /**
     * Test successful user signup
     * 
     * Validates:
     * - User is created with correct details
     * - Password is encoded
     * - UserResponse is returned
     */
    @Test
    @DisplayName("Should register new user successfully")
    void testSignup_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.signup(signupRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test signup with duplicate username
     * 
     * Validates:
     * - DuplicateResourceException is thrown
     * - User is not created
     */
    @Test
    @DisplayName("Should throw exception when username already exists")
    void testSignup_DuplicateUsername() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(signupRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("username");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test signup with duplicate email
     * 
     * Validates:
     * - DuplicateResourceException is thrown
     * - User is not created
     */
    @Test
    @DisplayName("Should throw exception when email already exists")
    void testSignup_DuplicateEmail() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(signupRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test successful user login
     * 
     * Validates:
     * - Authentication is performed
     * - JWT token is generated
     * - AuthResponse with token and user details is returned
     */
    @Test
    @DisplayName("Should login user successfully")
    void testLogin_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("test-jwt-token");
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.of(testUser));

        AuthResponse response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo(testUser.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    /**
     * Test getting user profile
     * 
     * Validates:
     * - User profile is retrieved
     * - Correct user details are returned
     */
    @Test
    @DisplayName("Should get user profile successfully")
    void testGetProfile_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getProfile(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUserId);
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
    }

    /**
     * Test getting profile for non-existent user
     * 
     * Validates:
     * - ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetProfile_UserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(testUserId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }

    /**
     * Test updating user profile
     * 
     * Validates:
     * - Profile is updated with new details
     * - Updated user is saved
     * - UserResponse with updated details is returned
     */
    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_Success() {
        UserProfileUpdateRequest updateRequest = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email("updated@example.com")
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateProfile(testUserId, updateRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test updating profile with duplicate email
     * 
     * Validates:
     * - DuplicateResourceException is thrown
     * - Profile is not updated
     */
    @Test
    @DisplayName("Should throw exception when updating to existing email")
    void testUpdateProfile_DuplicateEmail() {
        UserProfileUpdateRequest updateRequest = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email("existing@example.com")
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(testUserId, updateRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test updating profile with same email
     * 
     * Validates:
     * - Update succeeds when email is unchanged
     * - No duplicate check is performed
     */
    @Test
    @DisplayName("Should update profile successfully when email is unchanged")
    void testUpdateProfile_SameEmail() {
        UserProfileUpdateRequest updateRequest = UserProfileUpdateRequest.builder()
            .fullName("Updated Name")
            .email(testUser.getEmail())
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateProfile(testUserId, updateRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    /**
     * Test getting user by username
     * 
     * Validates:
     * - User is retrieved by username
     * - Correct user is returned
     */
    @Test
    @DisplayName("Should get user by username successfully")
    void testGetUserByUsername_Success() {
        when(userRepository.findByUsername(testUser.getUsername()))
            .thenReturn(Optional.of(testUser));

        User result = userService.getUserByUsername(testUser.getUsername());

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
    }

    /**
     * Test getting user by non-existent username
     * 
     * Validates:
     * - ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw exception when username not found")
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }
}