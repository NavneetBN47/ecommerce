package com.ecommerce.usermanagement.presentation.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.service.UserService;
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

import static org.mockito.ArgumentMatchers.*;
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
    private PasswordChangeRequest passwordChangeRequest;
    private PasswordResetRequest passwordResetRequest;
    private MessageResponse messageResponse;

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
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(1L)
                .email("test@example.com")
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

        messageResponse = MessageResponse.builder()
                .message("Operation completed successfully")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Valid Registration - Success")
    void testRegisterUser_ValidRequest_ReturnsCreated() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(registrationResponse);

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email - Returns Bad Request")
    void testRegisterUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                .email("invalid-email")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Required Fields - Returns Bad Request")
    void testRegisterUser_MissingFields_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password - Returns Bad Request")
    void testRegisterUser_WeakPassword_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("weak")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Valid Credentials - Success")
    void testLoginUser_ValidCredentials_ReturnsToken() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Email Format - Returns Bad Request")
    void testLoginUser_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        UserLoginRequest invalidRequest = UserLoginRequest.builder()
                .email("invalid-email")
                .password("SecurePass123!")
                .build();

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Empty Password - Returns Bad Request")
    void testLoginUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        UserLoginRequest invalidRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("GET /api/v1/users/profile - Authenticated User - Success")
    void testGetUserProfile_AuthenticatedUser_ReturnsProfile() throws Exception {
        when(userService.getUserProfile(1L)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/users/profile")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(userService, times(1)).getUserProfile(1L);
    }

    @Test
    @DisplayName("GET /api/v1/users/profile - Unauthenticated User - Returns Unauthorized")
    void testGetUserProfile_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("PUT /api/v1/users/profile - Valid Update - Success")
    void testUpdateUserProfile_ValidRequest_ReturnsUpdatedProfile() throws Exception {
        UserProfileResponse updatedProfile = UserProfileResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .build();

        when(userService.updateUserProfile(eq(1L), any(UserProfileUpdateRequest.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/v1/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        verify(userService, times(1)).updateUserProfile(eq(1L), any(UserProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("PUT /api/v1/users/profile - Invalid Phone Number - Returns Bad Request")
    void testUpdateUserProfile_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        UserProfileUpdateRequest invalidRequest = UserProfileUpdateRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("invalid-phone")
                .build();

        mockMvc.perform(put("/api/v1/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("POST /api/v1/users/password/change - Valid Request - Success")
    void testChangePassword_ValidRequest_ReturnsSuccess() throws Exception {
        when(userService.changePassword(eq(1L), any(PasswordChangeRequest.class)))
                .thenReturn(messageResponse);

        mockMvc.perform(post("/api/v1/users/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Operation completed successfully"));

        verify(userService, times(1)).changePassword(eq(1L), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("POST /api/v1/users/password/change - Weak New Password - Returns Bad Request")
    void testChangePassword_WeakNewPassword_ReturnsBadRequest() throws Exception {
        PasswordChangeRequest invalidRequest = PasswordChangeRequest.builder()
                .currentPassword("SecurePass123!")
                .newPassword("weak")
                .build();

        mockMvc.perform(post("/api/v1/users/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(anyLong(), any(PasswordChangeRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Valid Email - Success")
    void testResetPassword_ValidEmail_ReturnsSuccess() throws Exception {
        when(userService.resetPassword(any(PasswordResetRequest.class)))
                .thenReturn(messageResponse);

        mockMvc.perform(post("/api/v1/users/password/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Operation completed successfully"));

        verify(userService, times(1)).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Invalid Email Format - Returns Bad Request")
    void testResetPassword_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        PasswordResetRequest invalidRequest = PasswordResetRequest.builder()
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users/password/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(any(PasswordResetRequest.class));
    }
}