package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.domain.entity.User;
import com.ecommerce.usermanagement.domain.repository.UserRepository;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
import com.ecommerce.usermanagement.infrastructure.security.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
            .username("testuser")
            .email("test@example.com")
            .passwordHash(passwordService.hashPassword("SecurePass123!"))
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+1234567890")
            .address("123 Test St")
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.ACTIVE)
            .build();

        testUser = userRepository.save(testUser);
        authToken = jwtTokenService.generateToken(testUser.getId(), testUser.getEmail(), testUser.getRole().name());
    }

    @Test
    @DisplayName("POST /api/users/register - Should register new user successfully")
    void testRegisterUser_Success() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .password("SecurePass123!")
            .firstName("New")
            .lastName("User")
            .phoneNumber("+9876543210")
            .address("456 New St")
            .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.message").value(containsString("successfully")));
    }

    @Test
    @DisplayName("POST /api/users/register - Should return 400 for duplicate email")
    void testRegisterUser_DuplicateEmail() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .username("anotheruser")
            .email("test@example.com") // Duplicate email
            .password("SecurePass123!")
            .firstName("Another")
            .lastName("User")
            .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    @DisplayName("POST /api/users/register - Should return 400 for invalid input")
    void testRegisterUser_InvalidInput() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .username("ab") // Too short
            .email("invalid-email") // Invalid format
            .password("weak") // Too weak
            .firstName("")
            .lastName("")
            .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/login - Should login successfully")
    void testLoginUser_Success() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
            .email("test@example.com")
            .password("SecurePass123!")
            .build();

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userId").value(testUser.getId()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/users/login - Should return 401 for invalid credentials")
    void testLoginUser_InvalidCredentials() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
            .email("test@example.com")
            .password("WrongPassword123!")
            .build();

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }

    @Test
    @DisplayName("POST /api/users/login - Should return 401 for non-existent user")
    void testLoginUser_UserNotFound() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
            .email("nonexistent@example.com")
            .password("SecurePass123!")
            .build();

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/profile - Should get user profile successfully")
    void testGetUserProfile_Success() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(testUser.getId()))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.firstName").value("Test"))
            .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return 401 without token")
    void testGetUserProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return 401 with invalid token")
    void testGetUserProfile_InvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer invalid.token.here"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should update profile successfully")
    void testUpdateUserProfile_Success() throws Exception {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .firstName("Updated")
            .lastName("Name")
            .phoneNumber("+9876543210")
            .address("456 Updated St")
            .build();

        mockMvc.perform(put("/api/users/profile")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Updated"))
            .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should return 401 without token")
    void testUpdateUserProfile_Unauthorized() throws Exception {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
            .firstName("Updated")
            .build();

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/users/change-password - Should change password successfully")
    void testChangePassword_Success() throws Exception {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
            .currentPassword("SecurePass123!")
            .newPassword("NewSecurePass123!")
            .build();

        mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(containsString("successfully")));
    }

    @Test
    @DisplayName("POST /api/users/change-password - Should return 400 for incorrect current password")
    void testChangePassword_IncorrectCurrentPassword() throws Exception {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
            .currentPassword("WrongPassword123!")
            .newPassword("NewSecurePass123!")
            .build();

        mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("incorrect")));
    }

    @Test
    @DisplayName("POST /api/users/change-password - Should return 401 without token")
    void testChangePassword_Unauthorized() throws Exception {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
            .currentPassword("SecurePass123!")
            .newPassword("NewSecurePass123!")
            .build();

        mockMvc.perform(post("/api/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/users/reset-password - Should request password reset successfully")
    void testResetPassword_Success() throws Exception {
        PasswordResetRequest request = PasswordResetRequest.builder()
            .email("test@example.com")
            .build();

        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    @DisplayName("POST /api/users/reset-password - Should return 404 for non-existent email")
    void testResetPassword_EmailNotFound() throws Exception {
        PasswordResetRequest request = PasswordResetRequest.builder()
            .email("nonexistent@example.com")
            .build();

        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users/reset-password - Should return 400 for invalid email format")
    void testResetPassword_InvalidEmail() throws Exception {
        PasswordResetRequest request = PasswordResetRequest.builder()
            .email("invalid-email-format")
            .build();

        mockMvc.perform(post("/api/users/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}