package com.ecommerce.usermanagement.application.service;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.exception.*;
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
    private UserLoginRequest loginRequest;
    private UserProfileUpdateRequest profileUpdateRequest;
    private PasswordChangeRequest passwordChangeRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .build();

        profileUpdateRequest = UserProfileUpdateRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .build();

        passwordChangeRequest = PasswordChangeRequest.builder()
                .currentPassword("SecurePass123!")
                .newPassword("NewSecurePass456!")
                .build();
    }

    @Test
    @DisplayName("Register User - Valid Request - Success")
    void testRegisterUser_ValidRequest_Success() {
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordService.hashPassword(registrationRequest.getPassword()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserRegistrationResponse response = userService.registerUser(registrationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getMessage()).contains("successfully");

        verify(userRepository, times(1)).existsByEmail(registrationRequest.getEmail());
        verify(passwordService, times(1)).hashPassword(registrationRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logUserRegistration(anyLong(), anyString());
    }

    @Test
    @DisplayName("Register User - Email Already Exists - Throws Exception")
    void testRegisterUser_EmailExists_ThrowsException() {
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).existsByEmail(registrationRequest.getEmail());
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Weak Password - Throws Exception")
    void testRegisterUser_WeakPassword_ThrowsException() {
        UserRegistrationRequest weakPasswordRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("weak")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmail(weakPasswordRequest.getEmail())).thenReturn(false);
        when(passwordService.validatePasswordStrength(weakPasswordRequest.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.registerUser(weakPasswordRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password does not meet security requirements");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login User - Valid Credentials - Success")
    void testLoginUser_ValidCredentials_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(loginRequest.getPassword(), testUser.getPasswordHash()))
                .thenReturn(true);
        when(jwtTokenService.generateToken(testUser.getId(), testUser.getEmail()))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");

        UserLoginResponse response = userService.loginUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getExpiresIn()).isGreaterThan(0);

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordService, times(1)).verifyPassword(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtTokenService, times(1)).generateToken(testUser.getId(), testUser.getEmail());
        verify(auditLogService, times(1)).logUserLogin(anyLong(), anyString());
    }

    @Test
    @DisplayName("Login User - Invalid Email - Throws Exception")
    void testLoginUser_InvalidEmail_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Login User - Invalid Password - Throws Exception")
    void testLoginUser_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(loginRequest.getPassword(), testUser.getPasswordHash()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordService, times(1)).verifyPassword(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtTokenService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Login User - Inactive Account - Throws Exception")
    void testLoginUser_InactiveAccount_ThrowsException() {
        testUser.setActive(false);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Account is inactive");

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Get User Profile - Valid User ID - Success")
    void testGetUserProfile_ValidUserId_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileResponse response = userService.getUserProfile(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getPhoneNumber()).isEqualTo("+1234567890");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get User Profile - Invalid User ID - Throws Exception")
    void testGetUserProfile_InvalidUserId_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Update User Profile - Valid Request - Success")
    void testUpdateUserProfile_ValidRequest_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfileResponse response = userService.updateUserProfile(1L, profileUpdateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logProfileUpdate(anyLong());
    }

    @Test
    @DisplayName("Update User Profile - User Not Found - Throws Exception")
    void testUpdateUserProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserProfile(999L, profileUpdateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Change Password - Valid Request - Success")
    void testChangePassword_ValidRequest_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(passwordChangeRequest.getCurrentPassword(), testUser.getPasswordHash()))
                .thenReturn(true);
        when(passwordService.validatePasswordStrength(passwordChangeRequest.getNewPassword()))
                .thenReturn(true);
        when(passwordService.hashPassword(passwordChangeRequest.getNewPassword()))
                .thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = userService.changePassword(1L, passwordChangeRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("successfully");

        verify(userRepository, times(1)).findById(1L);
        verify(passwordService, times(1)).verifyPassword(passwordChangeRequest.getCurrentPassword(), testUser.getPasswordHash());
        verify(passwordService, times(1)).validatePasswordStrength(passwordChangeRequest.getNewPassword());
        verify(passwordService, times(1)).hashPassword(passwordChangeRequest.getNewPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logPasswordChange(anyLong());
    }

    @Test
    @DisplayName("Change Password - Incorrect Current Password - Throws Exception")
    void testChangePassword_IncorrectCurrentPassword_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(passwordChangeRequest.getCurrentPassword(), testUser.getPasswordHash()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, passwordChangeRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, times(1)).findById(1L);
        verify(passwordService, times(1)).verifyPassword(passwordChangeRequest.getCurrentPassword(), testUser.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Change Password - Weak New Password - Throws Exception")
    void testChangePassword_WeakNewPassword_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordService.verifyPassword(passwordChangeRequest.getCurrentPassword(), testUser.getPasswordHash()))
                .thenReturn(true);
        when(passwordService.validatePasswordStrength(passwordChangeRequest.getNewPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, passwordChangeRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password does not meet security requirements");

        verify(userRepository, times(1)).findById(1L);
        verify(passwordService, times(1)).verifyPassword(passwordChangeRequest.getCurrentPassword(), testUser.getPasswordHash());
        verify(passwordService, times(1)).validatePasswordStrength(passwordChangeRequest.getNewPassword());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Reset Password - Valid Email - Success")
    void testResetPassword_ValidEmail_Success() {
        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .email("test@example.com")
                .build();

        when(userRepository.findByEmail(resetRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        MessageResponse response = userService.resetPassword(resetRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("reset link");

        verify(userRepository, times(1)).findByEmail(resetRequest.getEmail());
        verify(auditLogService, times(1)).logPasswordResetRequest(anyLong());
    }

    @Test
    @DisplayName("Reset Password - Email Not Found - Throws Exception")
    void testResetPassword_EmailNotFound_ThrowsException() {
        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .email("nonexistent@example.com")
                .build();

        when(userRepository.findByEmail(resetRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(resetRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findByEmail(resetRequest.getEmail());
    }
}