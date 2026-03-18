package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.dto.AuthResponse;
import com.ecommerce.usermanagement.dto.LoginRequest;
import com.ecommerce.usermanagement.dto.RegisterRequest;
import com.ecommerce.usermanagement.dto.UserResponse;
import com.ecommerce.usermanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");

        authResponse = new AuthResponse();
        authResponse.setToken("jwt-token-123");
        authResponse.setEmail("test@example.com");
        authResponse.setUserId(1L);

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
    }

    @Test
    @DisplayName("POST /api/users/register - Success")
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/users/register - Invalid Email")
    void testRegisterUser_InvalidEmail() throws Exception {
        registerRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Weak Password")
    void testRegisterUser_WeakPassword() throws Exception {
        registerRequest.setPassword("weak");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Missing Required Fields")
    void testRegisterUser_MissingFields() throws Exception {
        registerRequest.setEmail(null);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/login - Success")
    void testLoginUser_Success() throws Exception {
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/users/login - Invalid Credentials")
    void testLoginUser_InvalidCredentials() throws Exception {
        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Success")
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer jwt-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - User Not Found")
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/999")
                .header("Authorization", "Bearer jwt-token-123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Unauthorized Access")
    void testGetUserById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Success")
    @WithMockUser
    void testUpdateUser_Success() throws Exception {
        when(userService.updateUser(anyLong(), any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                .header("Authorization", "Bearer jwt-token-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
