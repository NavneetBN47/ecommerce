package com.ecommerce.usermanagement.integration;

import com.ecommerce.usermanagement.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Management Integration Tests")
class UserManagementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accessToken;
    private static String userEmail = "integration.test@example.com";

    @Test
    @Order(1)
    @DisplayName("Should complete user registration flow")
    void testUserRegistration() throws Exception {
        UserRegistrationDTO registrationDTO = UserRegistrationDTO.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .firstName("Integration")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Test"));
    }

    @Test
    @Order(2)
    @DisplayName("Should prevent duplicate registration")
    void testDuplicateRegistration() throws Exception {
        UserRegistrationDTO registrationDTO = UserRegistrationDTO.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .firstName("Duplicate")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("Should complete user login flow")
    void testUserLogin() throws Exception {
        UserLoginDTO loginDTO = UserLoginDTO.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JwtTokenDTO tokenDTO = objectMapper.readValue(responseBody, JwtTokenDTO.class);
        accessToken = tokenDTO.getAccessToken();
    }

    @Test
    @Order(4)
    @DisplayName("Should reject login with invalid credentials")
    void testLoginWithInvalidCredentials() throws Exception {
        UserLoginDTO loginDTO = UserLoginDTO.builder()
                .email(userEmail)
                .password("WrongPassword123!")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("Should get user profile with valid token")
    void testGetUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.firstName").value("Integration"));
    }

    @Test
    @Order(6)
    @DisplayName("Should reject profile access without token")
    void testGetProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("Should update user profile")
    void testUpdateUserProfile() throws Exception {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    @Order(8)
    @DisplayName("Should change password successfully")
    void testChangePassword() throws Exception {
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword("SecurePass123!")
                .newPassword("NewSecurePass456!")
                .build();

        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    @DisplayName("Should login with new password")
    void testLoginWithNewPassword() throws Exception {
        UserLoginDTO loginDTO = UserLoginDTO.builder()
                .email(userEmail)
                .password("NewSecurePass456!")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JwtTokenDTO tokenDTO = objectMapper.readValue(responseBody, JwtTokenDTO.class);
        accessToken = tokenDTO.getAccessToken();
    }

    @Test
    @Order(10)
    @DisplayName("Should logout successfully")
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(11)
    @DisplayName("Should reject requests with blacklisted token")
    void testAccessWithBlacklistedToken() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(12)
    @DisplayName("Should delete user account")
    void testDeleteUserAccount() throws Exception {
        // Login again to get a new token
        UserLoginDTO loginDTO = UserLoginDTO.builder()
                .email(userEmail)
                .password("NewSecurePass456!")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JwtTokenDTO tokenDTO = objectMapper.readValue(responseBody, JwtTokenDTO.class);
        String newToken = tokenDTO.getAccessToken();

        mockMvc.perform(delete("/api/users/profile")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(13)
    @DisplayName("Should not find deleted user")
    void testLoginAfterDeletion() throws Exception {
        UserLoginDTO loginDTO = UserLoginDTO.builder()
                .email(userEmail)
                .password("NewSecurePass456!")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }
}