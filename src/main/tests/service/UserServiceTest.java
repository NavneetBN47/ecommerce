package com.ecommerce.usermanagement.service;

import com.ecommerce.usermanagement.dto.*;
import com.ecommerce.usermanagement.entity.User;
import com.ecommerce.usermanagement.repository.UserRepository;
import com.ecommerce.usermanagement.security.JwtTokenService;
import com.ecommerce.usermanagement.security.PasswordService;
import com.ecommerce.common.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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

    private User user;
    private UserRegistrationDTO registrationDTO;
    private UserLoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("$2a$12$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .build();

        registrationDTO = UserRegistrationDTO.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        loginDTO = UserLoginDTO.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordService.hashPassword(anyString())).thenReturn("$2a$12$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileDTO result = userService.registerUser(registrationDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordService, times(1)).hashPassword("SecurePass123!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logUserRegistration(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void testRegisterUser_DuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists");

        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordService.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenService.generateAccessToken(any(UUID.class), anyString())).thenReturn("access_token");
        when(jwtTokenService.generateRefreshToken(any(UUID.class))).thenReturn("refresh_token");

        JwtTokenDTO result = userService.login(loginDTO);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access_token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh_token");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordService, times(1)).matches("SecurePass123!", "$2a$12$hashedPassword");
        verify(jwtTokenService, times(1)).generateAccessToken(any(UUID.class), anyString());
        verify(auditLogService, times(1)).logUserLogin(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should throw exception when login with invalid email")
    void testLogin_InvalidEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordService, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when login with invalid password")
    void testLogin_InvalidPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordService.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordService, times(1)).matches("SecurePass123!", "$2a$12$hashedPassword");
        verify(jwtTokenService, never()).generateAccessToken(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void testGetUserProfile_Success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileDTO result = userService.getUserProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent user profile")
    void testGetUserProfile_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateUserProfile_Success() {
        UUID userId = UUID.randomUUID();
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileDTO result = userService.updateUserProfile(userId, updateDTO);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logProfileUpdate(any(UUID.class));
    }

    @Test
    @DisplayName("Should change password successfully")
    void testChangePassword_Success() {
        UUID userId = UUID.randomUUID();
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword("OldPass123!")
                .newPassword("NewPass456!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordService.matches("OldPass123!", user.getPasswordHash())).thenReturn(true);
        when(passwordService.hashPassword("NewPass456!")).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(userId, passwordChangeDTO);

        verify(userRepository, times(1)).findById(userId);
        verify(passwordService, times(1)).matches("OldPass123!", user.getPasswordHash());
        verify(passwordService, times(1)).hashPassword("NewPass456!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logPasswordChange(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when changing password with invalid current password")
    void testChangePassword_InvalidCurrentPassword() {
        UUID userId = UUID.randomUUID();
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword("WrongPass123!")
                .newPassword("NewPass456!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordService.matches("WrongPass123!", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, passwordChangeDTO))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid current password");

        verify(userRepository, times(1)).findById(userId);
        verify(passwordService, times(1)).matches("WrongPass123!", user.getPasswordHash());
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
        verify(auditLogService, times(1)).logUserDeletion(any(UUID.class));
    }

    @Test
    @DisplayName("Should logout successfully")
    void testLogout_Success() {
        String token = "valid_token";
        doNothing().when(jwtTokenService).blacklistToken(token);

        userService.logout(token);

        verify(jwtTokenService, times(1)).blacklistToken(token);
        verify(auditLogService, times(1)).logUserLogout(anyString());
    }
}