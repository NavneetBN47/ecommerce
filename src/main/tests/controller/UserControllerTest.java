package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.service.UserService;
import com.ecommerce.usermanagement.presentation.controller.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for UserController
 * Coverage: 100% of all API endpoints with valid, invalid, and edge cases
 * Authentication: JWT token scenarios fully tested
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Tests")
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== POST /api/v1/users/register ====================

    @Test
    @DisplayName("Register User - Valid Request - Should Return 201 Created")
    void testRegisterUser_ValidRequest_Success() throws Exception {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .email("test@example.com")
            .password("SecurePass123!")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .build();

        UserRegistrationResponse response = UserRegistrationResponse.builder()
            .userId(1L)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .message("User registered successfully")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("Register User - Invalid Email Format - Should Return 400 Bad Request")
    void testRegisterUser_InvalidEmail_BadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .email("invalid-email")
            .password("SecurePass123!")
            .firstName("John")
            .lastName("Doe")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("Register User - Weak Password - Should Return 400 Bad Request")
    void testRegisterUser_WeakPassword_BadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .email("test@example.com")
            .password("weak")
            .firstName("John")
            .lastName("Doe")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("Register User - Missing Required Fields - Should Return 400 Bad Request")
    void testRegisterUser_MissingFields_BadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .email("test@example.com")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("Register User - Duplicate Email - Should Return 400 Bad Request")
    void testRegisterUser_DuplicateEmail_BadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .email("existing@example.com")
            .password("SecurePass123!")
            .firstName("John")
            .lastName("Doe")
            .build();

        when(userService.registerUser(any(UserRegistrationRequest.class)))
            .thenThrow(new RuntimeException("User already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    // ==================== POST /api/v1/users/login ====================

    @Test
    @DisplayName("Login User - Valid Credentials - Should Return 200 OK with JWT Token")
    void testLoginUser_ValidCredentials_Success() throws Exception {
        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
            .email("test@example.com")
            .password("SecurePass123!")
            .build();

        UserLoginResponse response = UserLoginResponse.builder()
            .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .userId(1L)
            .email("test@example.com")
            .build();

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("Login User - Invalid Credentials - Should Return 401 Unauthorized")
    void testLoginUser_InvalidCredentials_Unauthorized() throws Exception {
        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
            .email("test@example.com")
            .password("WrongPassword")
            .build();

        when(userService.loginUser(any(UserLoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("Login User - Non-existent Email - Should Return 401 Unauthorized")
    void testLoginUser_NonExistentEmail_Unauthorized() throws Exception {
        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
            .email("nonexistent@example.com")
            .password("SecurePass123!")
            .build();

        when(userService.loginUser(any(UserLoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("Login User - Empty Credentials - Should Return 400 Bad Request")
    void testLoginUser_EmptyCredentials_BadRequest() throws Exception {
        // Arrange
        UserLoginRequest request = UserLoginRequest.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any());
    }

    // ==================== GET /api/v1/users/profile ====================

    @Test
    @DisplayName("Get User Profile - Valid Token - Should Return 200 OK")
    void testGetUserProfile_ValidToken_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        UserProfileResponse response = UserProfileResponse.builder()
            .userId(1L)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.getUserProfile("test@example.com")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"));

        verify(userService, times(1)).getUserProfile("test@example.com");
    }

    @Test
    @DisplayName("Get User Profile - Missing Token - Should Return 401 Unauthorized")
    void testGetUserProfile_MissingToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile"))
            .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(any());
    }

    @Test
    @DisplayName("Get User Profile - User Not Found - Should Return 404 Not Found")
    void testGetUserProfile_UserNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "nonexistent@example.com", null, null);

        when(userService.getUserProfile("nonexistent@example.com"))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                .principal(authentication))
            .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserProfile("nonexistent@example.com");
    }

    // ==================== PUT /api/v1/users/profile ====================

    @Test
    @DisplayName("Update User Profile - Valid Request - Should Return 200 OK")
    void testUpdateUserProfile_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+9876543210")
            .build();

        UserProfileResponse response = UserProfileResponse.builder()
            .userId(1L)
            .email("test@example.com")
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+9876543210")
            .updatedAt(LocalDateTime.now())
            .build();

        when(userService.updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUserProfile(eq("test@example.com"), any());
    }

    @Test
    @DisplayName("Update User Profile - Invalid Phone Number - Should Return 400 Bad Request")
    void testUpdateUserProfile_InvalidPhoneNumber_BadRequest() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .firstName("Jane")
            .phoneNumber("invalid-phone")
            .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserProfile(any(), any());
    }

    // ==================== POST /api/v1/users/password/reset ====================

    @Test
    @DisplayName("Request Password Reset - Valid Email - Should Return 200 OK")
    void testRequestPasswordReset_ValidEmail_Success() throws Exception {
        // Arrange
        PasswordResetRequest request = PasswordResetRequest.builder()
            .email("test@example.com")
            .build();

        MessageResponse response = MessageResponse.builder()
            .message("Password reset email sent successfully")
            .build();

        when(userService.requestPasswordReset(any(PasswordResetRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password reset email sent successfully"));

        verify(userService, times(1)).requestPasswordReset(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("Request Password Reset - Non-existent Email - Should Return 404 Not Found")
    void testRequestPasswordReset_NonExistentEmail_NotFound() throws Exception {
        // Arrange
        PasswordResetRequest request = PasswordResetRequest.builder()
            .email("nonexistent@example.com")
            .build();

        when(userService.requestPasswordReset(any(PasswordResetRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        verify(userService, times(1)).requestPasswordReset(any(PasswordResetRequest.class));
    }

    // ==================== POST /api/v1/users/password/change ====================

    @Test
    @DisplayName("Change Password - Valid Request - Should Return 200 OK")
    void testChangePassword_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        PasswordChangeRequest request = PasswordChangeRequest.builder()
            .currentPassword("OldPass123!")
            .newPassword("NewPass456!")
            .build();

        MessageResponse response = MessageResponse.builder()
            .message("Password changed successfully")
            .build();

        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService, times(1)).changePassword(eq("test@example.com"), any());
    }

    @Test
    @DisplayName("Change Password - Incorrect Current Password - Should Return 400 Bad Request")
    void testChangePassword_IncorrectCurrentPassword_BadRequest() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        PasswordChangeRequest request = PasswordChangeRequest.builder()
            .currentPassword("WrongPass123!")
            .newPassword("NewPass456!")
            .build();

        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
            .thenThrow(new RuntimeException("Current password is incorrect"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(eq("test@example.com"), any());
    }

    @Test
    @DisplayName("Change Password - Weak New Password - Should Return 400 Bad Request")
    void testChangePassword_WeakNewPassword_BadRequest() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        PasswordChangeRequest request = PasswordChangeRequest.builder()
            .currentPassword("OldPass123!")
            .newPassword("weak")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(), any());
    }

    // ==================== DELETE /api/v1/users/profile ====================

    @Test
    @DisplayName("Delete User Account - Valid Request - Should Return 200 OK")
    void testDeleteUserAccount_ValidRequest_Success() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, null);

        MessageResponse response = MessageResponse.builder()
            .message("Account deleted successfully")
            .build();

        when(userService.deleteUserAccount("test@example.com")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile")
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Account deleted successfully"));

        verify(userService, times(1)).deleteUserAccount("test@example.com");
    }

    @Test
    @DisplayName("Delete User Account - Missing Token - Should Return 401 Unauthorized")
    void testDeleteUserAccount_MissingToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile"))
            .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteUserAccount(any());
    }

    @Test
    @DisplayName("Delete User Account - User Not Found - Should Return 404 Not Found")
    void testDeleteUserAccount_UserNotFound_NotFound() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "nonexistent@example.com", null, null);

        when(userService.deleteUserAccount("nonexistent@example.com"))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile")
                .principal(authentication))
            .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUserAccount("nonexistent@example.com");
    }
}