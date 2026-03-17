package com.ecommerce.usermanagement.presentation.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.exception.*;
import com.ecommerce.usermanagement.application.service.UserService;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
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
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for UserController
 * Tests all endpoints with valid, invalid, and edge case scenarios
 * Coverage: 100% of API endpoints
 */
@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequest validRegistrationRequest;
    private UserRegistrationResponse registrationResponse;
    private UserLoginRequest validLoginRequest;
    private UserLoginResponse loginResponse;
    private UserProfileResponse profileResponse;
    private UserProfileUpdateRequest profileUpdateRequest;
    private PasswordChangeRequest passwordChangeRequest;
    private PasswordResetRequest passwordResetRequest;

    @BeforeEach
    void setUp() {
        // Setup valid registration request
        validRegistrationRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        registrationResponse = UserRegistrationResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .message("User registered successfully")
                .build();

        // Setup valid login request
        validLoginRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .build();

        loginResponse = UserLoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .userId(1L)
                .email("test@example.com")
                .expiresIn(3600L)
                .build();

        // Setup profile response
        profileResponse = UserProfileResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        // Setup profile update request
        profileUpdateRequest = UserProfileUpdateRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .build();

        // Setup password change request
        passwordChangeRequest = PasswordChangeRequest.builder()
                .currentPassword("SecurePass123!")
                .newPassword("NewSecurePass456!")
                .build();

        // Setup password reset request
        passwordResetRequest = PasswordResetRequest.builder()
                .email("test@example.com")
                .build();
    }

    // ==================== POST /api/v1/users/register Tests ====================

    @Test
    @DisplayName("POST /api/v1/users/register - Valid Registration - Should Return 201")
    void testRegisterUser_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(registrationResponse);

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email Format - Should Return 400")
    void testRegisterUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                .email("invalid-email")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password - Should Return 400")
    void testRegisterUser_WeakPassword_ReturnsBadRequest() throws Exception {
        // Given
        UserRegistrationRequest weakPasswordRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("weak")
                .firstName("John")
                .lastName("Doe")
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - User Already Exists - Should Return 409")
    void testRegisterUser_UserAlreadyExists_ReturnsConflict() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email test@example.com already exists"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isConflict());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Required Fields - Should Return 400")
    void testRegisterUser_MissingFields_ReturnsBadRequest() throws Exception {
        // Given
        UserRegistrationRequest incompleteRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    // ==================== POST /api/v1/users/login Tests ====================

    @Test
    @DisplayName("POST /api/v1/users/login - Valid Credentials - Should Return 200")
    void testLoginUser_ValidCredentials_ReturnsOk() throws Exception {
        // Given
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenReturn(loginResponse);

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Credentials - Should Return 401")
    void testLoginUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - User Not Found - Should Return 401")
    void testLoginUser_UserNotFound_ReturnsUnauthorized() throws Exception {
        // Given
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Empty Credentials - Should Return 400")
    void testLoginUser_EmptyCredentials_ReturnsBadRequest() throws Exception {
        // Given
        UserLoginRequest emptyRequest = UserLoginRequest.builder().build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    // ==================== GET /api/v1/users/profile/{userId} Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/users/profile/{userId} - Valid User ID - Should Return 200")
    void testGetUserProfile_ValidUserId_ReturnsOk() throws Exception {
        // Given
        when(userService.getUserProfile(anyLong())).thenReturn(profileResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/users/profile/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).getUserProfile(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/users/profile/{userId} - User Not Found - Should Return 404")
    void testGetUserProfile_UserNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.getUserProfile(anyLong()))
                .thenThrow(new UserNotFoundException("User not found with ID: 999"));

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/users/profile/999")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserProfile(999L);
    }

    @Test
    @DisplayName("GET /api/v1/users/profile/{userId} - No Authentication - Should Return 401")
    void testGetUserProfile_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/users/profile/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("GET /api/v1/users/profile/{userId} - Invalid User ID Format - Should Return 400")
    void testGetUserProfile_InvalidUserIdFormat_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/users/profile/invalid")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUserProfile(anyLong());
    }

    // ==================== PUT /api/v1/users/profile/{userId} Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("PUT /api/v1/users/profile/{userId} - Valid Update - Should Return 200")
    void testUpdateUserProfile_ValidRequest_ReturnsOk() throws Exception {
        // Given
        UserProfileResponse updatedProfile = UserProfileResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .build();

        when(userService.updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class)))
                .thenReturn(updatedProfile);

        // When
        ResultActions result = mockMvc.perform(put("/api/v1/users/profile/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        verify(userService, times(1)).updateUserProfile(eq(1L), any(UserProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("PUT /api/v1/users/profile/{userId} - User Not Found - Should Return 404")
    void testUpdateUserProfile_UserNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // When
        ResultActions result = mockMvc.perform(put("/api/v1/users/profile/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUserProfile(eq(999L), any(UserProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/profile/{userId} - No Authentication - Should Return 401")
    void testUpdateUserProfile_NoAuthentication_ReturnsUnauthorized() throws Exception {
        // When
        ResultActions result = mockMvc.perform(put("/api/v1/users/profile/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class));
    }

    // ==================== POST /api/v1/users/change-password Tests ====================

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/users/change-password - Valid Password Change - Should Return 200")
    void testChangePassword_ValidRequest_ReturnsOk() throws Exception {
        // Given
        MessageResponse messageResponse = MessageResponse.builder()
                .message("Password changed successfully")
                .build();

        when(userService.changePassword(anyLong(), any(PasswordChangeRequest.class)))
                .thenReturn(messageResponse);

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/change-password")
                .with(csrf())
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService, times(1)).changePassword(eq(1L), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/users/change-password - Invalid Current Password - Should Return 400")
    void testChangePassword_InvalidCurrentPassword_ReturnsBadRequest() throws Exception {
        // Given
        when(userService.changePassword(anyLong(), any(PasswordChangeRequest.class)))
                .thenThrow(new InvalidPasswordException("Current password is incorrect"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/change-password")
                .with(csrf())
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(eq(1L), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("POST /api/v1/users/change-password - Weak New Password - Should Return 400")
    void testChangePassword_WeakNewPassword_ReturnsBadRequest() throws Exception {
        // Given
        PasswordChangeRequest weakPasswordRequest = PasswordChangeRequest.builder()
                .currentPassword("SecurePass123!")
                .newPassword("weak")
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/change-password")
                .with(csrf())
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(anyLong(), any(PasswordChangeRequest.class));
    }

    // ==================== POST /api/v1/users/reset-password Tests ====================

    @Test
    @DisplayName("POST /api/v1/users/reset-password - Valid Email - Should Return 200")
    void testResetPassword_ValidEmail_ReturnsOk() throws Exception {
        // Given
        MessageResponse messageResponse = MessageResponse.builder()
                .message("Password reset email sent successfully")
                .build();

        when(userService.resetPassword(any(PasswordResetRequest.class)))
                .thenReturn(messageResponse);

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent successfully"));

        verify(userService, times(1)).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/reset-password - User Not Found - Should Return 404")
    void testResetPassword_UserNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.resetPassword(any(PasswordResetRequest.class)))
                .thenThrow(new UserNotFoundException("User not found with email"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/reset-password - Invalid Email Format - Should Return 400")
    void testResetPassword_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given
        PasswordResetRequest invalidRequest = PasswordResetRequest.builder()
                .email("invalid-email")
                .build();

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(any(PasswordResetRequest.class));
    }
}