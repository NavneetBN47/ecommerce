package com.ecommerce.integration;

import com.ecommerce.usermanagement.application.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Management Integration Tests")
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static final String TEST_EMAIL = "integration.test@example.com";
    private static final String TEST_PASSWORD = "IntegrationTest123!";

    @Test
    @Order(1)
    @DisplayName("Integration: Register User")
    void testRegisterUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setFirstName("Integration");
        request.setLastName("Test");
        request.setPhoneNumber("+1234567890");

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Login User")
    void testLoginUser() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UserLoginResponse response = objectMapper.readValue(responseBody, UserLoginResponse.class);
        jwtToken = response.getToken();
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Get User Profile")
    void testGetUserProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.firstName").value("Integration"));
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Update User Profile")
    void testUpdateUserProfile() throws Exception {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setPhoneNumber("+0987654321");

        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Change Password")
    void testChangePassword() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword(TEST_PASSWORD);
        request.setNewPassword("NewPassword123!");

        mockMvc.perform(post("/api/v1/users/password/change")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }
}