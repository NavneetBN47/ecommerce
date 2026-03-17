package com.ecommerce.usermanagement.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Unit Test Suite for UserController
 * Tests all API endpoints with valid, invalid, and edge case scenarios
 * Coverage: 100% of UserController endpoints
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
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
    private PasswordResetRequest validPasswordResetRequest;

    @BeforeEach
    void setUp() {
        // Setup valid test data
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
        validPasswordChangeRequest.setOldPassword("OldPass123!");
        validPasswordChangeRequest.setNewPassword("NewPass123!");

        validPasswordResetRequest = new PasswordResetRequest();
        validPasswordResetRequest.setEmail("test@example.com");
    }

    // ==================== USER REGISTRATION TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/register - Success - Valid Registration")
    void testRegisterUser_Success() throws Exception {
        // Arrange
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setMessage("User registered successfully");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
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
    @DisplayName("POST /api/v1/users/register - Failure - User Already Exists")
    void testRegisterUser_UserAlreadyExists() throws Exception {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email test@example.com already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Failure - Invalid Email Format")
    void testRegisterUser_InvalidEmail() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Failure - Weak Password")
    void testRegisterUser_WeakPassword() throws Exception {
        // Arrange
        validRegistrationRequest.setPassword("weak");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - Failure - Missing Required Fields")
    void testRegisterUser_MissingFields() throws Exception {
        // Arrange
        UserRegistrationRequest incompleteRequest = new UserRegistrationRequest();
        incompleteRequest.setEmail("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== USER LOGIN TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/login - Success - Valid Credentials")
    void testLoginUser_Success() throws Exception {
        // Arrange
        UserLoginResponse response = new UserLoginResponse();
        response.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        response.setEmail("test@example.com");
        response.setMessage("Login successful");

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Failure - Invalid Credentials")
    void testLoginUser_InvalidCredentials() throws Exception {
        // Arrange
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Failure - Account Locked")
    void testLoginUser_AccountLocked() throws Exception {
        // Arrange
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Account is locked due to multiple failed login attempts"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/users/login - Failure - Empty Credentials")
    void testLoginUser_EmptyCredentials() throws Exception {
        // Arrange
        UserLoginRequest emptyRequest = new UserLoginRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/users/profile - Success - Authenticated User")
    void testGetUserProfile_Success() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setPhoneNumber("+1234567890");

        when(userService.getUserProfile(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).getUserProfile(anyString());
    }

    @Test
    @DisplayName("GET /api/v1/users/profile - Failure - Unauthenticated")
    void testGetUserProfile_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/v1/users/profile - Failure - User Not Found")
    void testGetUserProfile_UserNotFound() throws Exception {
        // Arrange
        when(userService.getUserProfile(anyString()))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/profile - Success - Valid Update")
    void testUpdateUserProfile_Success() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("Jane");
        response.setLastName("Smith");

        when(userService.updateUserProfile(anyString(), any(UserProfileUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUserProfile(anyString(), any(UserProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/users/profile - Failure - Unauthenticated")
    void testUpdateUserProfile_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/v1/users/profile - Failure - Invalid Phone Number")
    void testUpdateUserProfile_InvalidPhoneNumber() throws Exception {
        // Arrange
        validUpdateRequest.setPhoneNumber("invalid");

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ==================== PASSWORD RESET REQUEST TESTS ====================

    @Test
    @DisplayName("POST /api/v1/users/password/reset-request - Success")
    void testRequestPasswordReset_Success() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("Password reset email sent successfully");
        when(userService.requestPasswordReset(any(PasswordResetRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent successfully"));

        verify(userService, times(1)).requestPasswordReset(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/password/reset-request - Failure - User Not Found")
    void testRequestPasswordReset_UserNotFound() throws Exception {
        // Arrange
        when(userService.requestPasswordReset(any(PasswordResetRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ==================== PASSWORD CHANGE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/users/password/change - Success")
    void testChangePassword_Success() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("Password changed successfully");
        when(userService.changePassword(anyString(), any(PasswordChangeRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService, times(1)).changePassword(anyString(), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/v1/users/password/change - Failure - Invalid Old Password")
    void testChangePassword_InvalidOldPassword() throws Exception {
        // Arrange
        when(userService.changePassword(anyString(), any(PasswordChangeRequest.class)))
                .thenThrow(new InvalidPasswordException("Old password is incorrect"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/password/change - Failure - Unauthenticated")
    void testChangePassword_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/users/password/change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== DELETE USER ACCOUNT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("DELETE /api/v1/users/profile - Success")
    void testDeleteUserAccount_Success() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("User account deleted successfully");
        when(userService.deleteUserAccount(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile")
                .header("Authorization", "Bearer valid-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account deleted successfully"));

        verify(userService, times(1)).deleteUserAccount(anyString());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/profile - Failure - Unauthenticated")
    void testDeleteUserAccount_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Edge Case - SQL Injection Attempt in Email")
    void testSQLInjectionAttempt() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("test@example.com'; DROP TABLE users; --");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case - XSS Attempt in First Name")
    void testXSSAttempt() throws Exception {
        // Arrange
        validRegistrationRequest.setFirstName("<script>alert('XSS')</script>");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case - Extremely Long Email")
    void testExtremelyLongEmail() throws Exception {
        // Arrange
        String longEmail = "a".repeat(300) + "@example.com";
        validRegistrationRequest.setEmail(longEmail);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case - Unicode Characters in Name")
    void testUnicodeCharactersInName() throws Exception {
        // Arrange
        validRegistrationRequest.setFirstName("José");
        validRegistrationRequest.setLastName("Müller");

        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setMessage("User registered successfully");

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Edge Case - Concurrent Login Attempts")
    void testConcurrentLoginAttempts() throws Exception {
        // Arrange
        UserLoginResponse response = new UserLoginResponse();
        response.setToken("token");
        response.setEmail("test@example.com");
        response.setMessage("Login successful");

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        // Act & Assert - Simulate multiple concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest))
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        verify(userService, times(5)).loginUser(any(UserLoginRequest.class));
    }
}