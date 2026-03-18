package com.ecommerce.usermanagement.application.service;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.exception.*;
import com.ecommerce.usermanagement.domain.entity.User;
import com.ecommerce.usermanagement.domain.repository.UserRepository;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
import com.ecommerce.usermanagement.infrastructure.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("+1234567890");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("SecurePass123!");
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setPhoneNumber("+1234567890");
    }

    @Test
    @DisplayName("Register User - Success")
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordService.validatePassword(anyString())).thenReturn(true);
        when(passwordService.hashPassword(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserRegistrationResponse response = userService.registerUser(registrationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getMessage()).contains("registered successfully");

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordService, times(1)).validatePassword("SecurePass123!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logUserRegistration(anyLong(), anyString());
    }

    @Test
    @DisplayName("Register User - User Already Exists")
    void testRegisterUser_UserAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Invalid Password")
    void testRegisterUser_InvalidPassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordService.validatePassword(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("complexity requirements");

        verify(passwordService, times(1)).validatePassword("SecurePass123!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login User - Success")
    void testLoginUser_Success() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("SecurePass123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(jwtTokenService.generateToken(anyString())).thenReturn("jwt.token.here");
        when(jwtTokenService.getExpirationTime()).thenReturn(3600L);

        UserLoginResponse response = userService.loginUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordService, times(1)).verifyPassword("SecurePass123!", "$2a$10$hashedPassword");
        verify(auditLogService, times(1)).logUserLogin(anyLong(), anyString());
    }

    @Test
    @DisplayName("Login User - User Not Found")
    void testLoginUser_UserNotFound() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("SecurePass123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Login User - Invalid Password")
    void testLoginUser_InvalidPassword() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("WrongPassword123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid");

        verify(passwordService, times(1)).verifyPassword("WrongPassword123!", "$2a$10$hashedPassword");
        verify(jwtTokenService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Login User - Inactive Account")
    void testLoginUser_InactiveAccount() {
        testUser.setIsActive(false);
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("SecurePass123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    @DisplayName("Get User Profile - Success")
    void testGetUserProfile_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = userService.getUserProfile("test@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Get User Profile - User Not Found")
    void testGetUserProfile_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Update User Profile - Success")
    void testUpdateUserProfile_Success() {
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhoneNumber("+0987654321");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfileResponse response = userService.updateUserProfile("test@example.com", updateRequest);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logProfileUpdate(anyLong(), anyString());
    }

    @Test
    @DisplayName("Change Password - Success")
    void testChangePassword_Success() {
        PasswordChangeRequest changeRequest = new PasswordChangeRequest();
        changeRequest.setCurrentPassword("OldPass123!");
        changeRequest.setNewPassword("NewPass123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(passwordService.validatePassword(anyString())).thenReturn(true);
        when(passwordService.hashPassword(anyString())).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = userService.changePassword("test@example.com", changeRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("changed successfully");

        verify(passwordService, times(1)).verifyPassword("OldPass123!", "$2a$10$hashedPassword");
        verify(passwordService, times(1)).validatePassword("NewPass123!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logPasswordChange(anyLong(), anyString());
    }

    @Test
    @DisplayName("Change Password - Invalid Current Password")
    void testChangePassword_InvalidCurrentPassword() {
        PasswordChangeRequest changeRequest = new PasswordChangeRequest();
        changeRequest.setCurrentPassword("WrongPass123!");
        changeRequest.setNewPassword("NewPass123!");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("test@example.com", changeRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("incorrect");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Change Password - Invalid New Password")
    void testChangePassword_InvalidNewPassword() {
        PasswordChangeRequest changeRequest = new PasswordChangeRequest();
        changeRequest.setCurrentPassword("OldPass123!");
        changeRequest.setNewPassword("weak");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(passwordService.validatePassword(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("test@example.com", changeRequest))
                .isInstanceOf(InvalidPasswordException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Reset Password - Success")
    void testResetPassword_Success() {
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        MessageResponse response = userService.resetPassword(resetRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("reset email sent");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(auditLogService, times(1)).logPasswordReset(anyLong(), anyString());
    }

    @Test
    @DisplayName("Reset Password - User Not Found (Silent Success)")
    void testResetPassword_UserNotFound() {
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        MessageResponse response = userService.resetPassword(resetRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("reset email sent");

        verify(auditLogService, never()).logPasswordReset(anyLong(), anyString());
    }
}