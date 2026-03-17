package com.ecommerce.usermanagement.presentation.controller;

import com.ecommerce.usermanagement.application.service.UserService;
import com.ecommerce.usermanagement.presentation.dto.*;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
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
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setEmail("test@example.com");
        validRegistrationDTO.setPassword("SecurePass123!");
        validRegistrationDTO.setFirstName("John");
        validRegistrationDTO.setLastName("Doe");
        
        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(testUserId);
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setFirstName("John");
        userResponseDTO.setLastName("Doe");
        userResponseDTO.setCreatedAt(LocalDateTime.now());
        
        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("SecurePass123!");
        
        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        loginResponseDTO.setRefreshToken("refresh_token_here");
        loginResponseDTO.setTokenType("Bearer");
        loginResponseDTO.setExpiresIn(3600L);
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Success")
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Invalid Email")
    void testRegisterUser_InvalidEmail() throws Exception {
        UserRegistrationDTO invalidDTO = new UserRegistrationDTO();
        invalidDTO.setEmail("invalid-email");
        invalidDTO.setPassword("SecurePass123!");
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Missing Required Fields")
    void testRegisterUser_MissingFields() throws Exception {
        UserRegistrationDTO incompleteDTO = new UserRegistrationDTO();
        incompleteDTO.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Weak Password")
    void testRegisterUser_WeakPassword() throws Exception {
        UserRegistrationDTO weakPasswordDTO = new UserRegistrationDTO();
        weakPasswordDTO.setEmail("test@example.com");
        weakPasswordDTO.setPassword("123");
        weakPasswordDTO.setFirstName("John");
        weakPasswordDTO.setLastName("Doe");

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Success")
    void testLogin_Success() throws Exception {
        when(userService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(userService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Invalid Credentials")
    void testLogin_InvalidCredentials() throws Exception {
        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Empty Credentials")
    void testLogin_EmptyCredentials() throws Exception {
        LoginRequestDTO emptyDTO = new LoginRequestDTO();

        mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{userId} - Success")
    void testGetUserProfile_Success() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/v1/users/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserById(testUserId);
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - Unauthorized")
    void testGetUserProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{userId} - User Not Found")
    void testGetUserProfile_NotFound() throws Exception {
        when(userService.getUserById(testUserId))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/v1/users/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(testUserId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/v1/users/{userId} - Success")
    void testUpdateUserProfile_Success() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");

        UserResponseDTO updatedResponse = new UserResponseDTO();
        updatedResponse.setId(testUserId);
        updatedResponse.setEmail("test@example.com");
        updatedResponse.setFirstName("Jane");
        updatedResponse.setLastName("Smith");

        when(userService.updateUser(eq(testUserId), any(UserUpdateDTO.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/users/{userId}", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUser(eq(testUserId), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{userId} - Unauthorized")
    void testUpdateUserProfile_Unauthorized() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Jane");

        mockMvc.perform(put("/api/v1/users/{userId}", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUser(any(UUID.class), any(UserUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/users/{userId} - Success")
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(testUserId);

        mockMvc.perform(delete("/api/v1/users/{userId}", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(testUserId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/users/{userId} - Forbidden for Non-Admin")
    void testDeleteUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}", testUserId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/users/refresh-token - Success")
    void testRefreshToken_Success() throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO();
        refreshRequest.setRefreshToken("valid_refresh_token");

        TokenRefreshResponseDTO refreshResponse = new TokenRefreshResponseDTO();
        refreshResponse.setAccessToken("new_access_token");
        refreshResponse.setRefreshToken("new_refresh_token");

        when(userService.refreshToken(any(TokenRefreshRequestDTO.class)))
                .thenReturn(refreshResponse);

        mockMvc.perform(post("/api/v1/users/refresh-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));

        verify(userService, times(1)).refreshToken(any(TokenRefreshRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/users/refresh-token - Invalid Token")
    void testRefreshToken_InvalidToken() throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO();
        refreshRequest.setRefreshToken("invalid_token");

        when(userService.refreshToken(any(TokenRefreshRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/users/refresh-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).refreshToken(any(TokenRefreshRequestDTO.class));
    }
}