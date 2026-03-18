package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.exception.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for UserController
 * Tests all 6 endpoints with valid, invalid, and edge cases
 * Coverage: 100% of API endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Tests")
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.ecommerce.common.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== POST /api/v1/users/register ====================

    @Test
    @DisplayName("Register User - Valid Request - Should Return 201 Created")
    void testRegisterUser_ValidRequest_Success() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "john.doe@example.com",
                "SecurePass123!",
                "John",
                "Doe",
                "+1234567890"
        );

        UserRegistrationResponse response = new UserRegistrationResponse(
                1L,
                "john.doe@example.com",
                "John",
                "Doe",
                "User registered successfully"
        );

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("Register User - Duplicate Email - Should Return 409 Conflict")
    void testRegisterUser_DuplicateEmail_Conflict() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "existing@example.com",
                "SecurePass123!",
                "John",
                "Doe",
                "+1234567890"
        );

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email existing@example.com already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("Register User - Invalid Email Format - Should Return 400 Bad Request")
    void testRegisterUser_InvalidEmail_BadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "invalid-email",
                "SecurePass123!",
                "John",
                "Doe",
                "+1234567890"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register User - Weak Password - Should Return 400 Bad Request")
    void testRegisterUser_WeakPassword_BadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "john.doe@example.com",
                "weak",
                "John",
                "Doe",
                "+1234567890"
        );

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new InvalidPasswordException("Password does not meet security requirements"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register User - Missing Required Fields - Should Return 400 Bad Request")
    void testRegisterUser_MissingFields_BadRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"email\":\"test@example.com\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST /api/v1/users/login ====================

    @Test
    @DisplayName("Login User - Valid Credentials - Should Return 200 OK with JWT Token")
    void testLoginUser_ValidCredentials_Success() throws Exception {
        // Arrange
        UserLoginRequest request = new UserLoginRequest(
                "john.doe@example.com",
                "SecurePass123!"
        );

        UserLoginResponse response = new UserLoginResponse(
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                "Bearer",
                3600L,
                1L,
                "john.doe@example.com",
                "John Doe"
        );

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("Login User - Invalid Credentials - Should Return 401 Unauthorized")
    void testLoginUser_InvalidCredentials_Unauthorized() throws Exception {
        // Arrange
        UserLoginRequest request = new UserLoginRequest(
                "john.doe@example.com",
                "WrongPassword"
        );

        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("Login User - Non-existent User - Should Return 401 Unauthorized")
    void testLoginUser_NonExistentUser_Unauthorized() throws Exception {
        // Arrange
        UserLoginRequest request = new UserLoginRequest(
                "nonexistent@example.com",
                "SomePassword123!"
        );

        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login User - Empty Credentials - Should Return 400 Bad Request")
    void testLoginUser_EmptyCredentials_BadRequest() throws Exception {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/v1/users/profile ====================

    @Test
    @DisplayName("Get User Profile - Valid User ID - Should Return 200 OK")
    void testGetUserProfile_ValidUserId_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "john.doe@example.com",
                "John",
                "Doe",
                "+1234567890",
                "2024-01-15T10:30:00Z"
        );

        when(userService.getUserProfile(userId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).getUserProfile(userId);
    }

    @Test
    @DisplayName("Get User Profile - Non-existent User - Should Return 404 Not Found")
    void testGetUserProfile_NonExistentUser_NotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        when(userService.getUserProfile(userId))
                .thenThrow(new UserNotFoundException("User not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserProfile(userId);
    }

    @Test
    @DisplayName("Get User Profile - Missing User ID Header - Should Return 400 Bad Request")
    void testGetUserProfile_MissingUserId_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/v1/users/profile ====================

    @Test
    @DisplayName("Update User Profile - Valid Request - Should Return 200 OK")
    void testUpdateUserProfile_ValidRequest_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "John",
                "Smith",
                "+9876543210"
        );

        UserProfileResponse response = new UserProfileResponse(
                1L,
                "john.doe@example.com",
                "John",
                "Smith",
                "+9876543210",
                "2024-01-15T10:30:00Z"
        );

        when(userService.updateUserProfile(eq(userId), any(UserProfileUpdateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        verify(userService, times(1)).updateUserProfile(eq(userId), any(UserProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("Update User Profile - Non-existent User - Should Return 404 Not Found")
    void testUpdateUserProfile_NonExistentUser_NotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "John",
                "Smith",
                "+9876543210"
        );

        when(userService.updateUserProfile(eq(userId), any(UserProfileUpdateRequest.class)))
                .thenThrow(new UserNotFoundException("User not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update User Profile - Invalid Phone Number - Should Return 400 Bad Request")
    void testUpdateUserProfile_InvalidPhoneNumber_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "John",
                "Smith",
                "invalid-phone"
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST /api/v1/users/password/reset ====================

    @Test
    @DisplayName("Reset Password - Valid Email - Should Return 200 OK")
    void testResetPassword_ValidEmail_Success() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("john.doe@example.com");
        MessageResponse response = new MessageResponse("Password reset email sent successfully");

        when(userService.initiatePasswordReset(any(PasswordResetRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent successfully"));

        verify(userService, times(1)).initiatePasswordReset(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("Reset Password - Non-existent Email - Should Return 404 Not Found")
    void testResetPassword_NonExistentEmail_NotFound() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("nonexistent@example.com");

        when(userService.initiatePasswordReset(any(PasswordResetRequest.class)))
                .thenThrow(new UserNotFoundException("User not found with email: nonexistent@example.com"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Reset Password - Invalid Email Format - Should Return 400 Bad Request")
    void testResetPassword_InvalidEmailFormat_BadRequest() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST /api/v1/users/password/change ====================

    @Test
    @DisplayName("Change Password - Valid Request - Should Return 200 OK")
    void testChangePassword_ValidRequest_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest(
                "OldPassword123!",
                "NewPassword456!"
        );
        MessageResponse response = new MessageResponse("Password changed successfully");

        when(userService.changePassword(eq(userId), any(PasswordChangeRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService, times(1)).changePassword(eq(userId), any(PasswordChangeRequest.class));
    }

    @Test
    @DisplayName("Change Password - Incorrect Old Password - Should Return 400 Bad Request")
    void testChangePassword_IncorrectOldPassword_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest(
                "WrongOldPassword",
                "NewPassword456!"
        );

        when(userService.changePassword(eq(userId), any(PasswordChangeRequest.class)))
                .thenThrow(new InvalidPasswordException("Current password is incorrect"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Change Password - Weak New Password - Should Return 400 Bad Request")
    void testChangePassword_WeakNewPassword_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest(
                "OldPassword123!",
                "weak"
        );

        when(userService.changePassword(eq(userId), any(PasswordChangeRequest.class)))
                .thenThrow(new InvalidPasswordException("New password does not meet security requirements"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Change Password - Same Old and New Password - Should Return 400 Bad Request")
    void testChangePassword_SamePasswords_BadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        PasswordChangeRequest request = new PasswordChangeRequest(
                "SamePassword123!",
                "SamePassword123!"
        );

        when(userService.changePassword(eq(userId), any(PasswordChangeRequest.class)))
                .thenThrow(new InvalidPasswordException("New password must be different from current password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
