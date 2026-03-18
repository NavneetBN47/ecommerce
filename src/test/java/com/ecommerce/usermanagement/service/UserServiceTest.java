package com.ecommerce.usermanagement.service;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.exception.*;
import com.ecommerce.usermanagement.application.mapper.UserMapper;
import com.ecommerce.usermanagement.application.service.AuditLogService;
import com.ecommerce.usermanagement.application.service.UserService;
import com.ecommerce.usermanagement.domain.entity.User;
import com.ecommerce.usermanagement.domain.repository.UserRepository;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
import com.ecommerce.usermanagement.infrastructure.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private User user;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = UserRegistrationRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("SecurePass123!")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+1234567890")
            .address("123 Test St")
            .build();

        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("$2a$12$hashedPassword")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+1234567890")
            .address("123 Test St")
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        loginRequest = UserLoginRequest.builder()
            .email("test@example.com")
            .password("SecurePass123!")
            .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordService.hashPassword(anyString())).thenReturn("$2a$12$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toRegistrationResponse(any(User.class)))
            .thenReturn(UserRegistrationResponse.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .message("User registered successfully")
                .build());

        // Act
        UserRegistrationResponse response = userService.registerUser(registrationRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logUserRegistration(1L, "test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterUser_EmailExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("email");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("username");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void testLoginUser_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(jwtTokenService.generateToken(anyLong(), anyString(), anyString()))
            .thenReturn("jwt.token.here");

        // Act
        UserLoginResponse response = userService.loginUser(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(auditLogService).logSuccessfulLogin(1L, "test@example.com");
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void testLoginUser_InvalidCredentials() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Invalid email or password");
        verify(auditLogService).logFailedLogin(1L, "test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLoginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void testGetUserProfile_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toProfileResponse(any(User.class)))
            .thenReturn(UserProfileResponse.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build());

        // Act
        UserProfileResponse response = userService.getUserProfile(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when profile not found")
    void testGetUserProfile_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserProfile(1L))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateUserProfile_Success() {
        // Arrange
        UserProfileUpdateRequest updateRequest = UserProfileUpdateRequest.builder()
            .firstName("Updated")
            .lastName("Name")
            .phoneNumber("+9876543210")
            .address("456 New St")
            .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileResponse(any(User.class)))
            .thenReturn(UserProfileResponse.builder()
                .userId(1L)
                .firstName("Updated")
                .lastName("Name")
                .build());

        // Act
        UserProfileResponse response = userService.updateUserProfile(1L, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logProfileUpdate(1L, "test@example.com");
    }

    @Test
    @DisplayName("Should change password successfully")
    void testChangePassword_Success() {
        // Arrange
        PasswordChangeRequest changeRequest = PasswordChangeRequest.builder()
            .currentPassword("SecurePass123!")
            .newPassword("NewSecurePass123!")
            .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(passwordService.hashPassword(anyString())).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        MessageResponse response = userService.changePassword(1L, changeRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("successfully");
        verify(auditLogService).logPasswordChange(1L, "test@example.com");
    }

    @Test
    @DisplayName("Should throw exception for incorrect current password")
    void testChangePassword_IncorrectCurrentPassword() {
        // Arrange
        PasswordChangeRequest changeRequest = PasswordChangeRequest.builder()
            .currentPassword("WrongPassword")
            .newPassword("NewSecurePass123!")
            .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.changePassword(1L, changeRequest))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("Current password is incorrect");
    }

    @Test
    @DisplayName("Should request password reset successfully")
    void testResetPassword_Success() {
        // Arrange
        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
            .email("test@example.com")
            .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        MessageResponse response = userService.resetPassword(resetRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("email");
        verify(auditLogService).logPasswordResetRequest(1L, "test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when reset email not found")
    void testResetPassword_EmailNotFound() {
        // Arrange
        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
            .email("nonexistent@example.com")
            .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.resetPassword(resetRequest))
            .isInstanceOf(UserNotFoundException.class);
    }
}