package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.dto.*;
import com.ecommerce.usermanagement.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRegistrationRequest validRegistrationRequest;
    private UserRegistrationResponse registrationResponse;
    private UserLoginRequest validLoginRequest;
    private UserLoginResponse loginResponse;
    private UserProfileResponse profileResponse;
    private UserProfileUpdateRequest profileUpdateRequest;
    private PasswordResetRequest passwordResetRequest;
    private PasswordChangeRequest passwordChangeRequest;

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
            .message("User registered successfully")
            .build();

        // Setup valid login request
        validLoginRequest = UserLoginRequest.builder()
            .email("test@example.com")
            .password("SecurePass123!")
            .build();

        loginResponse = UserLoginResponse.builder()
            .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            .refreshToken("refresh_token_here")
            .tokenType("Bearer")
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

        // Setup password reset request
        passwordResetRequest = PasswordResetRequest.builder()
            .email("test@example.com")
            .build();

        // Setup password change request
        passwordChangeRequest = PasswordChangeRequest.builder()
            .currentPassword("OldPass123!")
            .newPassword("NewPass456!")
            .confirmPassword("NewPass456!")
            .build();
    }

    // ==================== REGISTER USER TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/register - Valid Request - Should Return 201")
    void testRegisterUser_ValidRequest_ReturnsCreated() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
            .thenReturn(registrationResponse);

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRegistrationRequest)));

        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email - Should Return 400")
    void testRegisterUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
            .email("invalid-email")
            .password("SecurePass123!")
            .firstName("John")
            .lastName("Doe")
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password - Should Return 400")
    void testRegisterUser_WeakPassword_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest weakPasswordRequest = UserRegistrationRequest.builder()
            .email("test@example.com")
            .password("weak")
            .firstName("John")
            .lastName("Doe")
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(weakPasswordRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Required Fields - Should Return 400")
    void testRegisterUser_MissingFields_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest incompleteRequest = UserRegistrationRequest.builder()
            .email("test@example.com")
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(incompleteRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Duplicate Email - Should Return 400")
    void testRegisterUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRegistrationRequest)));

        result.andExpect(status().isInternalServerError());
        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    // ==================== LOGIN USER TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/login - Valid Credentials - Should Return 200")
    void testLoginUser_ValidCredentials_ReturnsOk() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
            .thenReturn(loginResponse);

        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validLoginRequest)));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Credentials - Should Return 401")
    void testLoginUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validLoginRequest)));

        result.andExpect(status().isInternalServerError());
        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Missing Email - Should Return 400")
    void testLoginUser_MissingEmail_ReturnsBadRequest() throws Exception {
        UserLoginRequest invalidRequest = UserLoginRequest.builder()
            .password("SecurePass123!")
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Empty Password - Should Return 400")
    void testLoginUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        UserLoginRequest invalidRequest = UserLoginRequest.builder()
            .email("test@example.com")
            .password("")
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    // ==================== GET PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/users/profile - Authenticated User - Should Return 200")
    void testGetUserProfile_AuthenticatedUser_ReturnsOk() throws Exception {
        when(userService.getUserProfile("test@example.com"))
            .thenReturn(profileResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/users/profile")
            .with(csrf()));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).getUserProfile("test@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/users/profile - Unauthenticated User - Should Return 401")
    void testGetUserProfile_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/users/profile"));

        result.andExpect(status().isUnauthorized());
        verify(userService, never()).getUserProfile(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/users/profile - User Not Found - Should Return 404")
    void testGetUserProfile_UserNotFound_ReturnsNotFound() throws Exception {
        when(userService.getUserProfile("test@example.com"))
            .thenThrow(new RuntimeException("User not found"));

        ResultActions result = mockMvc.perform(get("/api/v1/users/profile")
            .with(csrf()));

        result.andExpect(status().isInternalServerError());
        verify(userService, times(1)).getUserProfile("test@example.com");
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/profile - Valid Update - Should Return 200")
    void testUpdateUserProfile_ValidUpdate_ReturnsOk() throws Exception {
        UserProfileResponse updatedProfile = UserProfileResponse.builder()
            .userId(1L)
            .email("test@example.com")
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("+9876543210")
            .build();

        when(userService.updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class)))
            .thenReturn(updatedProfile);

        ResultActions result = mockMvc.perform(put("/api/v1/users/profile")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileUpdateRequest)));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Smith"))
            .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        verify(userService, times(1)).updateUserProfile(eq("test@example.com"), any(UserProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/profile - Unauthenticated - Should Return 401")
    void testUpdateUserProfile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(put("/api/v1/users/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileUpdateRequest)));

        result.andExpect(status().isForbidden());
        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/profile - Invalid Phone Number - Should Return 400")
    void testUpdateUserProfile_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        UserProfileUpdateRequest invalidRequest = UserProfileUpdateRequest.builder()
            .firstName("Jane")
            .lastName("Smith")
            .phoneNumber("invalid-phone")
            .build();

        ResultActions result = mockMvc.perform(put("/api/v1/users/profile")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).updateUserProfile(any(), any());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/users/logout - Authenticated User - Should Return 204")
    void testLogoutUser_AuthenticatedUser_ReturnsNoContent() throws Exception {
        doNothing().when(userService).logoutUser("test@example.com");

        ResultActions result = mockMvc.perform(post("/api/v1/users/logout")
            .with(csrf()));

        result.andExpect(status().isNoContent());
        verify(userService, times(1)).logoutUser("test@example.com");
    }

    @Test
    @DisplayName("POST /api/v1/users/logout - Unauthenticated - Should Return 401")
    void testLogoutUser_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/users/logout"));

        result.andExpect(status().isForbidden());
        verify(userService, never()).logoutUser(any());
    }

    // ==================== PASSWORD RESET TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Valid Email - Should Return 200")
    void testRequestPasswordReset_ValidEmail_ReturnsOk() throws Exception {
        doNothing().when(userService).requestPasswordReset(any(PasswordResetRequest.class));

        ResultActions result = mockMvc.perform(post("/api/v1/users/password/reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordResetRequest)));

        result.andExpect(status().isOk());
        verify(userService, times(1)).requestPasswordReset(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Invalid Email Format - Should Return 400")
    void testRequestPasswordReset_InvalidEmail_ReturnsBadRequest() throws Exception {
        PasswordResetRequest invalidRequest = PasswordResetRequest.builder()
            .email("invalid-email")
            .build();

        ResultActions result = mockMvc.perform(post("/api/v1/users/password/reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).requestPasswordReset(any());
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Email Not Found - Should Return 404")
    void testRequestPasswordReset_EmailNotFound_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("Email not found"))
            .when(userService).requestPasswordReset(any(PasswordResetRequest.class));

        ResultActions result = mockMvc.perform(post("/api/v1/users/password/reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordResetRequest)));

        result.andExpect(status().isInternalServerError());
        verify(userService, times(1)).requestPasswordReset(any(PasswordResetRequest.class));
    }

    // ==================== PASSWORD CHANGE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/password/change - Valid Request - Should Return 200")
    void testChangePassword_ValidRequest_ReturnsOk() throws Exception {
        doNothing().when(userService).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));

        ResultActions result = mockMvc.perform(put("/api/v1/users/password/change")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordChangeRequest)));

        result.andExpect(status().isOk());
        verify(userService, times(1)).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/password/change - Unauthenticated - Should Return 401")
    void testChangePassword_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ResultActions result = mockMvc.perform(put("/api/v1/users/password/change")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordChangeRequest)));

        result.andExpect(status().isForbidden());
        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/password/change - Password Mismatch - Should Return 400")
    void testChangePassword_PasswordMismatch_ReturnsBadRequest() throws Exception {
        PasswordChangeRequest mismatchRequest = PasswordChangeRequest.builder()
            .currentPassword("OldPass123!")
            .newPassword("NewPass456!")
            .confirmPassword("DifferentPass789!")
            .build();

        ResultActions result = mockMvc.perform(put("/api/v1/users/password/change")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mismatchRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/password/change - Incorrect Current Password - Should Return 400")
    void testChangePassword_IncorrectCurrentPassword_ReturnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Current password is incorrect"))
            .when(userService).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));

        ResultActions result = mockMvc.perform(put("/api/v1/users/password/change")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordChangeRequest)));

        result.andExpect(status().isInternalServerError());
        verify(userService, times(1)).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/password/change - Weak New Password - Should Return 400")
    void testChangePassword_WeakNewPassword_ReturnsBadRequest() throws Exception {
        PasswordChangeRequest weakPasswordRequest = PasswordChangeRequest.builder()
            .currentPassword("OldPass123!")
            .newPassword("weak")
            .confirmPassword("weak")
            .build();

        ResultActions result = mockMvc.perform(put("/api/v1/users/password/change")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(weakPasswordRequest)));

        result.andExpect(status().isBadRequest());
        verify(userService, never()).changePassword(any(), any());
    }
}