package com.ecommerce.usermanagement.presentation.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.service.UserService;
import com.ecommerce.usermanagement.application.exception.*;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("User Controller Tests")
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
    private UserProfileUpdateRequest validUpdateRequest;
    private PasswordChangeRequest validPasswordChangeRequest;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new UserRegistrationRequest();
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("SecurePass123!");
        validRegistrationRequest.setFirstName("John");
        validRegistrationRequest.setLastName("Doe");
        validRegistrationRequest.setPhoneNumber("+1234567890");

        validLoginRequest = new UserLoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("SecurePass123!");

        validUpdateRequest = new UserProfileUpdateRequest();
        validUpdateRequest.setFirstName("Jane");
        validUpdateRequest.setLastName("Smith");
        validUpdateRequest.setPhoneNumber("+0987654321");

        validPasswordChangeRequest = new PasswordChangeRequest();
        validPasswordChangeRequest.setCurrentPassword("OldPass123!");
        validPasswordChangeRequest.setNewPassword("NewPass123!");
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Success")
    void testRegisterUser_Success() throws Exception {
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setMessage("User registered successfully");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - User Already Exists")
    void testRegisterUser_UserAlreadyExists() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email test@example.com already exists"));

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email test@example.com already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email Format")
    void testRegisterUser_InvalidEmail() throws Exception {
        validRegistrationRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password")
    void testRegisterUser_WeakPassword() throws Exception {
        validRegistrationRequest.setPassword("weak");

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new InvalidPasswordException("Password does not meet complexity requirements"));

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Required Fields")
    void testRegisterUser_MissingFields() throws Exception {
        validRegistrationRequest.setEmail(null);
        validRegistrationRequest.setPassword(null);

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Success")
    void testLoginUser_Success() throws Exception {
        UserLoginResponse response = new UserLoginResponse();
        response.setToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setExpiresIn(3600L);

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Credentials")
    void testLoginUser_InvalidCredentials() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - User Not Found")
    void testLoginUser_UserNotFound() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/users/profile - Success")
    void testGetUserProfile_Success() throws Exception {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");

        when(userService.getUserProfile(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/profile - Unauthorized")
    void testGetUserProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/profile - Success")
    void testUpdateUserProfile_Success() throws Exception {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setFirstName("Jane");
        response.setLastName("Smith");

        when(userService.updateUserProfile(anyString(), any(UserProfileUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/users/password/change - Success")
    void testChangePassword_Success() throws Exception {
        MessageResponse response = new MessageResponse("Password changed successfully");

        when(userService.changePassword(anyString(), any(PasswordChangeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/password/change")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/users/password/change - Invalid Current Password")
    void testChangePassword_InvalidCurrentPassword() throws Exception {
        when(userService.changePassword(anyString(), any(PasswordChangeRequest.class)))
                .thenThrow(new InvalidCredentialsException("Current password is incorrect"));

        mockMvc.perform(post("/api/v1/users/password/change")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset - Success")
    void testResetPassword_Success() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("test@example.com");

        MessageResponse response = new MessageResponse("Password reset email sent");

        when(userService.resetPassword(any(PasswordResetRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/password/reset")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"));
    }
}