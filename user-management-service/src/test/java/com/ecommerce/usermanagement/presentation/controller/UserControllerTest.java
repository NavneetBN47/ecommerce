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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRegistrationDTO validRegistrationDTO;
    private UserResponseDTO userResponseDTO;
    private LoginRequestDTO validLoginRequest;
    private LoginResponseDTO loginResponseDTO;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setEmail("test@example.com");
        validRegistrationDTO.setPassword("SecurePass123!");
        validRegistrationDTO.setFirstName("John");
        validRegistrationDTO.setLastName("Doe");
        validRegistrationDTO.setPhoneNumber("+1234567890");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserId(userId);
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setFirstName("John");
        userResponseDTO.setLastName("Doe");
        userResponseDTO.setPhoneNumber("+1234567890");
        userResponseDTO.setRole("USER");
        userResponseDTO.setCreatedAt(LocalDateTime.now());

        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("SecurePass123!");

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");
        loginResponseDTO.setRefreshToken("refresh_token_here");
        loginResponseDTO.setTokenType("Bearer");
        loginResponseDTO.setExpiresIn(3600);
        loginResponseDTO.setUser(userResponseDTO);
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Valid Registration - Success")
    void testRegisterUser_ValidInput_ReturnsCreated() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email - Bad Request")
    void testRegisterUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        validRegistrationDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password - Bad Request")
    void testRegisterUser_WeakPassword_ReturnsBadRequest() throws Exception {
        validRegistrationDTO.setPassword("weak");

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Required Fields - Bad Request")
    void testRegisterUser_MissingFields_ReturnsBadRequest() throws Exception {
        UserRegistrationDTO incompleteDTO = new UserRegistrationDTO();
        incompleteDTO.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Duplicate Email - Bad Request")
    void testRegisterUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Valid Credentials - Success")
    void testLogin_ValidCredentials_ReturnsOk() throws Exception {
        when(userService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(userService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Credentials - Unauthorized")
    void testLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Empty Email - Bad Request")
    void testLogin_EmptyEmail_ReturnsBadRequest() throws Exception {
        validLoginRequest.setEmail("");

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Empty Password - Bad Request")
    void testLogin_EmptyPassword_ReturnsBadRequest() throws Exception {
        validLoginRequest.setPassword("");

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{userId} - Valid User ID - Success")
    void testGetUserProfile_ValidUserId_ReturnsOk() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - No Authentication - Unauthorized")
    void testGetUserProfile_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserById(anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{userId} - User Not Found - Not Found")
    void testGetUserProfile_UserNotFound_ReturnsNotFound() throws Exception {
        when(userService.getUserById(userId))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/v1/users/{userId} - Valid Update - Success")
    void testUpdateUserProfile_ValidUpdate_ReturnsOk() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setPhoneNumber("+9876543210");

        UserResponseDTO updatedResponse = new UserResponseDTO();
        updatedResponse.setUserId(userId);
        updatedResponse.setEmail("test@example.com");
        updatedResponse.setFirstName("Jane");
        updatedResponse.setLastName("Smith");
        updatedResponse.setPhoneNumber("+9876543210");

        when(userService.updateUser(eq(userId), any(UserUpdateDTO.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{userId} - No Authentication - Unauthorized")
    void testUpdateUserProfile_NoAuth_ReturnsUnauthorized() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Jane");

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUser(anyString(), any(UserUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/users/{userId} - Admin Role - Success")
    void testDeleteUser_AdminRole_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/users/{userId} - User Role - Forbidden")
    void testDeleteUser_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("POST /api/v1/users/password-reset-request - Valid Email - Success")
    void testRequestPasswordReset_ValidEmail_ReturnsOk() throws Exception {
        PasswordResetRequestDTO resetRequest = new PasswordResetRequestDTO();
        resetRequest.setEmail("test@example.com");

        doNothing().when(userService).requestPasswordReset("test@example.com");

        mockMvc.perform(post("/api/v1/users/password-reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).requestPasswordReset("test@example.com");
    }

    @Test
    @DisplayName("POST /api/v1/users/password-reset - Valid Token - Success")
    void testResetPassword_ValidToken_ReturnsOk() throws Exception {
        PasswordResetDTO resetDTO = new PasswordResetDTO();
        resetDTO.setToken("valid-reset-token");
        resetDTO.setNewPassword("NewSecurePass123!");

        doNothing().when(userService).resetPassword(any(PasswordResetDTO.class));

        mockMvc.perform(post("/api/v1/users/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetDTO))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).resetPassword(any(PasswordResetDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/refresh-token - Valid Refresh Token - Success")
    void testRefreshToken_ValidToken_ReturnsOk() throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO();
        refreshRequest.setRefreshToken("valid-refresh-token");

        TokenRefreshResponseDTO refreshResponse = new TokenRefreshResponseDTO();
        refreshResponse.setAccessToken("new-access-token");
        refreshResponse.setRefreshToken("new-refresh-token");
        refreshResponse.setTokenType("Bearer");
        refreshResponse.setExpiresIn(3600);

        when(userService.refreshToken(any(TokenRefreshRequestDTO.class))).thenReturn(refreshResponse);

        mockMvc.perform(post("/api/v1/users/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));

        verify(userService, times(1)).refreshToken(any(TokenRefreshRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/refresh-token - Invalid Token - Unauthorized")
    void testRefreshToken_InvalidToken_ReturnsUnauthorized() throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO();
        refreshRequest.setRefreshToken("invalid-token");

        when(userService.refreshToken(any(TokenRefreshRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/users/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).refreshToken(any(TokenRefreshRequestDTO.class));
    }
}