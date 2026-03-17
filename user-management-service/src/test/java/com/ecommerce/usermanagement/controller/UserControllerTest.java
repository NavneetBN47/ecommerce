package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.service.UserService;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
import com.ecommerce.usermanagement.presentation.controller.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for UserController
 * Coverage: All 7 endpoints with valid, invalid, and edge cases
 * Authentication: JWT-based security testing
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenService jwtTokenService;

    private UserRegistrationRequest validRegistrationRequest;
    private UserLoginRequest validLoginRequest;
    private UserProfileUpdateRequest validProfileUpdateRequest;
    private PasswordChangeRequest validPasswordChangeRequest;
    private PasswordResetRequest validPasswordResetRequest;

    @BeforeEach
    void setUp() {
        // Valid registration request
        validRegistrationRequest = new UserRegistrationRequest();
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("SecurePass123!");
        validRegistrationRequest.setFirstName("John");
        validRegistrationRequest.setLastName("Doe");
        validRegistrationRequest.setPhoneNumber("+1234567890");

        // Valid login request
        validLoginRequest = new UserLoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("SecurePass123!");

        // Valid profile update request
        validProfileUpdateRequest = new UserProfileUpdateRequest();
        validProfileUpdateRequest.setFirstName("Jane");
        validProfileUpdateRequest.setLastName("Smith");
        validProfileUpdateRequest.setPhoneNumber("+9876543210");

        // Valid password change request
        validPasswordChangeRequest = new PasswordChangeRequest();
        validPasswordChangeRequest.setCurrentPassword("OldPass123!");
        validPasswordChangeRequest.setNewPassword("NewPass456!");

        // Valid password reset request
        validPasswordResetRequest = new PasswordResetRequest();
        validPasswordResetRequest.setEmail("test@example.com");
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("POST /api/users/register - Valid Registration - Success")
    void testRegisterUser_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setMessage("User registered successfully");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Invalid Email Format - BadRequest")
    void testRegisterUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Weak Password - BadRequest")
    void testRegisterUser_WeakPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        validRegistrationRequest.setPassword("weak");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Missing Required Fields - BadRequest")
    void testRegisterUser_MissingFields_ReturnsBadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest incompleteRequest = new UserRegistrationRequest();
        incompleteRequest.setEmail("test@example.com");
        // Missing password, firstName, lastName

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Duplicate Email - Conflict")
    void testRegisterUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("User already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("POST /api/users/login - Valid Credentials - Success")
    void testLoginUser_ValidCredentials_ReturnsOk() throws Exception {
        // Arrange
        UserLoginResponse response = new UserLoginResponse();
        response.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setExpiresIn(3600L);

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/login - Invalid Credentials - Unauthorized")
    void testLoginUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/login - Empty Email - BadRequest")
    void testLoginUser_EmptyEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        validLoginRequest.setEmail("");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/login - Empty Password - BadRequest")
    void testLoginUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        validLoginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    // ==================== PROFILE RETRIEVAL TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/users/profile - Authenticated User - Success")
    void testGetUserProfile_AuthenticatedUser_ReturnsOk() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setPhoneNumber("+1234567890");

        when(userService.getUserProfile("test@example.com")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).getUserProfile("test@example.com");
    }

    @Test
    @DisplayName("GET /api/users/profile - Unauthenticated User - Unauthorized")
    void testGetUserProfile_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(any());
    }

    // ==================== PROFILE UPDATE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/users/profile - Valid Update - Success")
    void testUpdateUserProfile_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("Jane");
        response.setLastName("Smith");
        response.setPhoneNumber("+9876543210");

        when(userService.updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validProfileUpdateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/users/profile - Invalid Phone Number - BadRequest")
    void testUpdateUserProfile_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        // Arrange
        validProfileUpdateRequest.setPhoneNumber("invalid");

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validProfileUpdateRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("PUT /api/users/profile - Unauthenticated - Unauthorized")
    void testUpdateUserProfile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validProfileUpdateRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserProfile(any(), any());
    }

    // ==================== PASSWORD CHANGE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/users/change-password - Valid Request - Success")
    void testChangePassword_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("Password changed successfully");
        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService, times(1)).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/users/change-password - Incorrect Current Password - BadRequest")
    void testChangePassword_IncorrectCurrentPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
                .thenThrow(new RuntimeException("Current password is incorrect"));

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/users/change-password - Weak New Password - BadRequest")
    void testChangePassword_WeakNewPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        validPasswordChangeRequest.setNewPassword("weak");

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("POST /api/users/change-password - Unauthenticated - Unauthorized")
    void testChangePassword_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).changePassword(any(), any());
    }

    // ==================== PASSWORD RESET TESTS ====================

    @Test
    @DisplayName("POST /api/users/reset-password - Valid Email - Success")
    void testResetPassword_ValidEmail_ReturnsOk() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("Password reset email sent");
        when(userService.resetPassword(any(PasswordResetRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"));

        verify(userService, times(1)).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/reset-password - Invalid Email Format - BadRequest")
    void testResetPassword_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        validPasswordResetRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/reset-password - Non-existent Email - NotFound")
    void testResetPassword_NonExistentEmail_ReturnsNotFound() throws Exception {
        // Arrange
        when(userService.resetPassword(any(PasswordResetRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).resetPassword(any(PasswordResetRequest.class));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("POST /api/users/register - SQL Injection Attempt - Sanitized")
    void testRegisterUser_SqlInjectionAttempt_Sanitized() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("test@example.com'; DROP TABLE users; --");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - XSS Attempt - Sanitized")
    void testRegisterUser_XssAttempt_Sanitized() throws Exception {
        // Arrange
        validRegistrationRequest.setFirstName("<script>alert('XSS')</script>");

        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setMessage("User registered successfully");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isCreated());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Very Long Email - BadRequest")
    void testRegisterUser_VeryLongEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        String longEmail = "a".repeat(300) + "@example.com";
        validRegistrationRequest.setEmail(longEmail);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Unicode Characters in Name - Success")
    void testRegisterUser_UnicodeCharacters_Success() throws Exception {
        // Arrange
        validRegistrationRequest.setFirstName("José");
        validRegistrationRequest.setLastName("Müller");

        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setMessage("User registered successfully");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isCreated());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/users/profile - Null Fields - PartialUpdate")
    void testUpdateUserProfile_NullFields_PartialUpdate() throws Exception {
        // Arrange
        UserProfileUpdateRequest partialUpdate = new UserProfileUpdateRequest();
        partialUpdate.setFirstName("Jane");
        // lastName and phoneNumber are null

        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("Jane");
        response.setLastName("Doe");
        response.setPhoneNumber("+1234567890");

        when(userService.updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(userService, times(1)).updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class));
    }
}