package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
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
 * This test class verifies the business logic for user management,
 * including signup, login, profile operations, and authentication.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_UserService {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;
    private UserSignupRequest signupRequest;
    private UserLoginRequest loginRequest;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .username("testuser")
                .password("encodedPassword")
                .fullName("Test User")
                .email("test@example.com")
                .isActive(true)
                .emailVerified(false)
                .build();

        signupRequest = new UserSignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");
        signupRequest.setEmail("test@example.com");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    /**
     * Test successful user signup
     * 
     * Verifies that a new user can be registered successfully
     * with password encoding and proper field initialization.
     */
    @Test
    void signup_WithValidRequest_ShouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.signup(signupRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getEmailVerified()).isFalse();
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test signup with duplicate username throws exception
     * 
     * Verifies that attempting to register with existing username
     * throws DuplicateResourceException.
     */
    @Test
    void signup_WithDuplicateUsername_ShouldThrowException() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test signup with duplicate email throws exception
     * 
     * Verifies that attempting to register with existing email
     * throws DuplicateResourceException.
     */
    @Test
    void signup_WithDuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test signup encodes password
     * 
     * Verifies that user password is properly encoded before saving.
     */
    @Test
    void signup_ShouldEncodePassword() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.signup(signupRequest);

        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
    }

    /**
     * Test successful user login
     * 
     * Verifies that a user can login successfully and
     * receives JWT token with user details.
     */
    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));

        AuthResponse response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).generateToken(authentication);
    }

    /**
     * Test login with non-existent user throws exception
     * 
     * Verifies that attempting to login with non-existent username
     * throws ResourceNotFoundException.
     */
    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    /**
     * Test successful profile retrieval
     * 
     * Verifies that a user can retrieve their profile successfully.
     */
    @Test
    void getProfile_WithValidUserId_ShouldReturnUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test profile retrieval with non-existent user throws exception
     * 
     * Verifies that attempting to get profile for non-existent user
     * throws ResourceNotFoundException.
     */
    @Test
    void getProfile_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    /**
     * Test successful profile update
     * 
     * Verifies that a user can update their profile successfully.
     */
    @Test
    void updateProfile_WithValidRequest_ShouldUpdateUserSuccessfully() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateProfile(userId, request);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test profile update with duplicate email throws exception
     * 
     * Verifies that attempting to update email to an existing one
     * throws DuplicateResourceException.
     */
    @Test
    void updateProfile_WithDuplicateEmail_ShouldThrowException() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("existing@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test profile update with same email does not throw exception
     * 
     * Verifies that updating profile with the same email
     * does not trigger duplicate check.
     */
    @Test
    void updateProfile_WithSameEmail_ShouldNotCheckDuplicate() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateProfile(userId, request);

        assertThat(response).isNotNull();
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test profile update with non-existent user throws exception
     * 
     * Verifies that attempting to update profile for non-existent user
     * throws ResourceNotFoundException.
     */
    @Test
    void updateProfile_WithNonExistentUser_ShouldThrowException() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    /**
     * Test get user by username
     * 
     * Verifies that a user can be retrieved by username successfully.
     */
    @Test
    void getUserByUsername_WithValidUsername_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    /**
     * Test get user by username with non-existent user throws exception
     * 
     * Verifies that attempting to get user by non-existent username
     * throws ResourceNotFoundException.
     */
    @Test
    void getUserByUsername_WithNonExistentUsername_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }
}