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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        // Setup valid registration request
        validRegistrationRequest = new UserRegistrationRequest(
            "john.doe@example.com",
            "SecurePass123!",
            "John",
            "Doe"
        );

        // Setup registration response
        registrationResponse = new UserRegistrationResponse(
            UUID.randomUUID(),
            "john.doe@example.com",
            "John",
            "Doe",
            LocalDateTime.now(),
            "User registered successfully"
        );

        // Setup valid login request
        validLoginRequest = new UserLoginRequest(
            "john.doe@example.com",
            "SecurePass123!"
        );

        // Setup login response
        loginResponse = new UserLoginResponse(
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test.token",
            "Bearer",
            86400000L,
            UUID.randomUUID(),
            "john.doe@example.com",
            "John",
            "Doe"
        );

        // Setup profile response
        profileResponse = new UserProfileResponse(
            UUID.randomUUID(),
            "john.doe@example.com",
            "John",
            "Doe",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Setup profile update request
        profileUpdateRequest = new UserProfileUpdateRequest(
            "Jane",
            "Smith"
        );

        // Setup password reset request
        passwordResetRequest = new PasswordResetRequest(
            "john.doe@example.com"
        );

        // Setup password change request
        passwordChangeRequest = new PasswordChangeRequest(
            "OldPass123!",
            "NewSecurePass456!"
        );

        // Setup message response
        messageResponse = new MessageResponse("Operation completed successfully");
    }

    // ==================== REGISTER USER TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/register - Valid Request - Should Return 201")
    void testRegisterUser_ValidRequest_ShouldReturn201() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
            .thenReturn(registrationResponse);

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email - Should Return 400")
    void testRegisterUser_InvalidEmail_ShouldReturn400() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "invalid-email",
            "SecurePass123!",
            "John",
            "Doe"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password - Should Return 400")
    void testRegisterUser_WeakPassword_ShouldReturn400() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "john.doe@example.com",
            "weak",
            "John",
            "Doe"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing First Name - Should Return 400")
    void testRegisterUser_MissingFirstName_ShouldReturn400() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "john.doe@example.com",
            "SecurePass123!",
            "",
            "Doe"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Last Name - Should Return 400")
    void testRegisterUser_MissingLastName_ShouldReturn400() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "john.doe@example.com",
            "SecurePass123!",
            "John",
            ""
        );

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ==================== LOGIN USER TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/login - Valid Credentials - Should Return 200")
    void testLoginUser_ValidCredentials_ShouldReturn200() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
            .thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(86400000L))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Email Format - Should Return 400")
    void testLoginUser_InvalidEmailFormat_ShouldReturn400() throws Exception {
        UserLoginRequest invalidRequest = new UserLoginRequest(
            "invalid-email",
            "SecurePass123!"
        );

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Missing Password - Should Return 400")
    void testLoginUser_MissingPassword_ShouldReturn400() throws Exception {
        UserLoginRequest invalidRequest = new UserLoginRequest(
            "john.doe@example.com",
            ""
        );

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ==================== GET USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("GET /api/v1/users/profile - Authenticated User - Should Return 200")
    void testGetUserProfile_AuthenticatedUser_ShouldReturn200() throws Exception {
        when(userService.getUserProfile(eq("john.doe@example.com")))
            .thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/users/profile")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/users/profile - Unauthenticated User - Should Return 401")
    void testGetUserProfile_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/users/profile - Valid Update - Should Return 200")
    void testUpdateUserProfile_ValidUpdate_ShouldReturn200() throws Exception {
        UserProfileResponse updatedProfile = new UserProfileResponse(
            UUID.randomUUID(),
            "john.doe@example.com",
            "Jane",
            "Smith",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(userService.updateUserProfile(eq("john.doe@example.com"), any(UserProfileUpdateRequest.class)))
            .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/users/profile - Missing First Name - Should Return 400")
    void testUpdateUserProfile_MissingFirstName_ShouldReturn400() throws Exception {
        UserProfileUpdateRequest invalidRequest = new UserProfileUpdateRequest(
            "",
            "Smith"
        );

        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/profile - Unauthenticated User - Should Return 401")
    void testUpdateUserProfile_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ==================== LOGOUT USER TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("POST /api/v1/users/logout - Authenticated User - Should Return 200")
    void testLogoutUser_AuthenticatedUser_ShouldReturn200() throws Exception {
        when(userService.logoutUser(eq("john.doe@example.com")))
            .thenReturn(messageResponse);

        mockMvc.perform(post("/api/v1/users/logout")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Operation completed successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/users/logout - Unauthenticated User - Should Return 401")
    void testLogoutUser_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/users/logout")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    // ==================== PASSWORD RESET TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Valid Email - Should Return 200")
    void testRequestPasswordReset_ValidEmail_ShouldReturn200() throws Exception {
        when(userService.requestPasswordReset(any(PasswordResetRequest.class)))
            .thenReturn(messageResponse);

        mockMvc.perform(post("/api/v1/users/password/reset")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Invalid Email Format - Should Return 400")
    void testRequestPasswordReset_InvalidEmailFormat_ShouldReturn400() throws Exception {
        PasswordResetRequest invalidRequest = new PasswordResetRequest("invalid-email");

        mockMvc.perform(post("/api/v1/users/password/reset")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ==================== PASSWORD CHANGE TESTS ====================

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/users/password/change - Valid Request - Should Return 200")
    void testChangePassword_ValidRequest_ShouldReturn200() throws Exception {
        when(userService.changePassword(eq("john.doe@example.com"), any(PasswordChangeRequest.class)))
            .thenReturn(messageResponse);

        mockMvc.perform(put("/api/v1/users/password/change")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("PUT /api/v1/users/password/change - Weak New Password - Should Return 400")
    void testChangePassword_WeakNewPassword_ShouldReturn400() throws Exception {
        PasswordChangeRequest invalidRequest = new PasswordChangeRequest(
            "OldPass123!",
            "weak"
        );

        mockMvc.perform(put("/api/v1/users/password/change")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/users/password/change - Unauthenticated User - Should Return 401")
    void testChangePassword_UnauthenticatedUser_ShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/v1/users/password/change")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequest)))
            .andExpect(status().isUnauthorized());
    }
}