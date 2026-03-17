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
 * Comprehensive Unit Tests for UserController
 * Tests all API endpoints with valid, invalid, and edge case scenarios
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@DisplayName("User Controller Unit Tests")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

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
        validPasswordChangeRequest.setCurrentPassword("OldPass123!");
        validPasswordChangeRequest.setNewPassword("NewPass456!");

        validPasswordResetRequest = new PasswordResetRequest();
        validPasswordResetRequest.setEmail("test@example.com");
    }

    // ==================== USER REGISTRATION TESTS ====================

    @Test
    @DisplayName("POST /api/users/register - Valid Registration - Should Return 201")
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
    @DisplayName("POST /api/users/register - Invalid Email Format - Should Return 400")
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
    @DisplayName("POST /api/users/register - Missing Required Fields - Should Return 400")
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
    @DisplayName("POST /api/users/register - Weak Password - Should Return 400")
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
    @DisplayName("POST /api/users/register - Duplicate Email - Should Return 409")
    void testRegisterUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("User already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isConflict());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    // ==================== USER LOGIN TESTS ====================

    @Test
    @DisplayName("POST /api/users/login - Valid Credentials - Should Return 200 with Token")
    void testLoginUser_ValidCredentials_ReturnsToken() throws Exception {
        // Arrange
        UserLoginResponse response = new UserLoginResponse();
        response.setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L);
        response.setUserId(1L);
        response.setEmail("test@example.com");

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/login - Invalid Credentials - Should Return 401")
    void testLoginUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/login - Missing Email - Should Return 400")
    void testLoginUser_MissingEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        validLoginRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginUser(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/login - Empty Password - Should Return 400")
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

    // ==================== GET USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/users/profile - Authenticated User - Should Return 200")
    void testGetUserProfile_AuthenticatedUser_ReturnsProfile() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");

        when(userService.getUserProfile(eq(1L))).thenReturn(response);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserProfile(eq(1L));
    }

    @Test
    @DisplayName("GET /api/users/profile - No Authentication - Should Return 401")
    void testGetUserProfile_NoAuth_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/users/profile - Invalid Token - Should Return 401")
    void testGetUserProfile_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(jwtTokenService.extractUserId(anyString()))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyLong());
    }

    // ==================== UPDATE USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("PUT /api/users/profile - Valid Update - Should Return 200")
    void testUpdateUserProfile_ValidRequest_ReturnsUpdated() throws Exception {
        // Arrange
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(1L);
        response.setFirstName("Jane");
        response.setLastName("Smith");

        when(userService.updateUserProfile(eq(1L), any(UserProfileUpdateRequest.class)))
                .thenReturn(response);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUserProfile(eq(1L), any(UserProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users/profile - No Authentication - Should Return 401")
    void testUpdateUserProfile_NoAuth_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class));
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/users/change-password - Valid Request - Should Return 200")
    void testChangePassword_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("Password changed successfully");
        when(userService.changePassword(eq(1L), any(PasswordChangeRequest.class)))
                .thenReturn(response);
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService, times(1)).changePassword(eq(1L), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/users/change-password - Wrong Current Password - Should Return 400")
    void testChangePassword_WrongCurrentPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        when(userService.changePassword(eq(1L), any(PasswordChangeRequest.class)))
                .thenThrow(new RuntimeException("Current password is incorrect"));
        when(jwtTokenService.extractUserId(anyString())).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordChangeRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(eq(1L), any(PasswordChangeRequest.class));
    }

    // ==================== PASSWORD RESET TESTS ====================

    @Test
    @DisplayName("POST /api/users/reset-password - Valid Email - Should Return 200")
    void testResetPassword_ValidEmail_ReturnsSuccess() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse("Password reset email sent");
        when(userService.initiatePasswordReset(any(PasswordResetRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"));

        verify(userService, times(1)).initiatePasswordReset(any(PasswordResetRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/reset-password - Invalid Email - Should Return 404")
    void testResetPassword_InvalidEmail_ReturnsNotFound() throws Exception {
        // Arrange
        when(userService.initiatePasswordReset(any(PasswordResetRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPasswordResetRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).initiatePasswordReset(any(PasswordResetRequest.class));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("POST /api/users/register - SQL Injection Attempt - Should Be Sanitized")
    void testRegisterUser_SQLInjection_ShouldBeSanitized() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("test@example.com'; DROP TABLE users;--");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - XSS Attempt - Should Be Sanitized")
    void testRegisterUser_XSSAttempt_ShouldBeSanitized() throws Exception {
        // Arrange
        validRegistrationRequest.setFirstName("<script>alert('XSS')</script>");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Extremely Long Input - Should Return 400")
    void testRegisterUser_ExtremelyLongInput_ReturnsBadRequest() throws Exception {
        // Arrange
        String longString = "a".repeat(1000);
        validRegistrationRequest.setFirstName(longString);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Null Request Body - Should Return 400")
    void testRegisterUser_NullBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/register - Malformed JSON - Should Return 400")
    void testRegisterUser_MalformedJSON_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }
}