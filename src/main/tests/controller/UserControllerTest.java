package com.ecommerce.usermanagement.controller;

import com.ecommerce.usermanagement.dto.*;
import com.ecommerce.usermanagement.service.UserService;
import com.ecommerce.usermanagement.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    private UserRegistrationDTO validRegistrationDTO;
    private UserLoginDTO validLoginDTO;
    private UserProfileDTO userProfileDTO;
    private JwtTokenDTO jwtTokenDTO;

    @BeforeEach
    void setUp() {
        validRegistrationDTO = UserRegistrationDTO.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        validLoginDTO = UserLoginDTO.builder()
                .email("test@example.com")
                .password("SecurePass123!")
                .build();

        userProfileDTO = UserProfileDTO.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        jwtTokenDTO = JwtTokenDTO.builder()
                .accessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("refresh_token_here")
                .expiresIn(3600L)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userProfileDTO);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when registering with invalid email")
    void testRegisterUser_InvalidEmail() throws Exception {
        UserRegistrationDTO invalidDTO = UserRegistrationDTO.builder()
                .email("invalid-email")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when registering with weak password")
    void testRegisterUser_WeakPassword() throws Exception {
        UserRegistrationDTO weakPasswordDTO = UserRegistrationDTO.builder()
                .email("test@example.com")
                .password("weak")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPasswordDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when registering with duplicate email")
    void testRegisterUser_DuplicateEmail() throws Exception {
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() throws Exception {
        when(userService.login(any(UserLoginDTO.class))).thenReturn(jwtTokenDTO);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(userService, times(1)).login(any(UserLoginDTO.class));
    }

    @Test
    @DisplayName("Should return 401 with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        when(userService.login(any(UserLoginDTO.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginDTO)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).login(any(UserLoginDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when login with missing fields")
    void testLogin_MissingFields() throws Exception {
        UserLoginDTO incompleteDTO = UserLoginDTO.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(UserLoginDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should get user profile successfully when authenticated")
    void testGetProfile_Success() throws Exception {
        when(userService.getUserProfile(any(UUID.class))).thenReturn(userProfileDTO);

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(userService, times(1)).getUserProfile(any(UUID.class));
    }

    @Test
    @DisplayName("Should return 401 when getting profile without authentication")
    void testGetProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(any(UUID.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should update profile successfully when authenticated")
    void testUpdateProfile_Success() throws Exception {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userService.updateUserProfile(any(UUID.class), any(UserUpdateDTO.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(userService, times(1)).updateUserProfile(any(UUID.class), any(UserUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 401 when updating profile without authentication")
    void testUpdateProfile_Unauthorized() throws Exception {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserProfile(any(UUID.class), any(UserUpdateDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should delete profile successfully when authenticated")
    void testDeleteProfile_Success() throws Exception {
        doNothing().when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/users/profile")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(any(UUID.class));
    }

    @Test
    @DisplayName("Should return 401 when deleting profile without authentication")
    void testDeleteProfile_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteUser(any(UUID.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should logout successfully when authenticated")
    void testLogout_Success() throws Exception {
        doNothing().when(userService).logout(anyString());

        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).logout(anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("Should change password successfully when authenticated")
    void testChangePassword_Success() throws Exception {
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword("OldPass123!")
                .newPassword("NewPass456!")
                .build();

        doNothing().when(userService).changePassword(any(UUID.class), any(PasswordChangeDTO.class));

        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDTO)))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).changePassword(any(UUID.class), any(PasswordChangeDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when changing password with invalid current password")
    void testChangePassword_InvalidCurrentPassword() throws Exception {
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword("WrongPass123!")
                .newPassword("NewPass456!")
                .build();

        doThrow(new InvalidPasswordException("Invalid current password"))
                .when(userService).changePassword(any(UUID.class), any(PasswordChangeDTO.class));

        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).changePassword(any(UUID.class), any(PasswordChangeDTO.class));
    }

    @Test
    @DisplayName("Should return 401 when changing password without authentication")
    void testChangePassword_Unauthorized() throws Exception {
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword("OldPass123!")
                .newPassword("NewPass456!")
                .build();

        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDTO)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).changePassword(any(UUID.class), any(PasswordChangeDTO.class));
    }
}