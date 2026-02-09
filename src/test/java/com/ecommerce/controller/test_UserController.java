package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserController
 * Tests all public endpoints for user management operations
 * Mocks UserService layer to isolate controller logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class test_UserController {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Test creating user successfully
     * Verifies that users are created with proper validation
     */
    @Test
    @DisplayName("Should successfully create new user")
    void testCreateUser_Success() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        
        UserDTO createdUser = new UserDTO();
        createdUser.setId(1L);
        createdUser.setUsername("testuser");
        createdUser.setEmail("test@example.com");
        createdUser.setFirstName("John");
        createdUser.setLastName("Doe");
        
        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).createUser(any(UserDTO.class));
    }

    /**
     * Test creating user with invalid data
     * Verifies that validation errors are handled properly
     */
    @Test
    @DisplayName("Should return validation error for invalid user data")
    void testCreateUser_InvalidData() throws Exception {
        // Given
        UserDTO invalidUser = new UserDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test getting user by ID successfully
     * Verifies that users can be retrieved by ID
     */
    @Test
    @DisplayName("Should successfully get user by ID")
    void testGetUserById_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        
        when(userService.getUserById(userId)).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserById(eq(userId));
    }

    /**
     * Test getting all users successfully
     * Verifies that all users can be retrieved
     */
    @Test
    @DisplayName("Should successfully get all users")
    void testGetAllUsers_Success() throws Exception {
        // Given
        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        
        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        List<UserDTO> users = Arrays.asList(user1, user2);
        
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("user1"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));

        verify(userService).getAllUsers();
    }

    /**
     * Test updating user successfully
     * Verifies that users can be updated
     */
    @Test
    @DisplayName("Should successfully update user")
    void testUpdateUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("updateduser");
        userDTO.setEmail("updated@example.com");
        userDTO.setFirstName("Jane");
        userDTO.setLastName("Smith");
        
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(userId);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        
        when(userService.updateUser(eq(userId), any(UserDTO.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));

        verify(userService).updateUser(eq(userId), any(UserDTO.class));
    }

    /**
     * Test deleting user successfully
     * Verifies that users can be deleted
     */
    @Test
    @DisplayName("Should successfully delete user")
    void testDeleteUser_Success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userService).deleteUser(eq(userId));
    }

    /**
     * Test getting non-existent user
     * Verifies that proper error handling occurs for invalid user IDs
     */
    @Test
    @DisplayName("Should handle getting non-existent user")
    void testGetUserById_NotFound() throws Exception {
        // Given
        Long nonExistentUserId = 999L;
        
        when(userService.getUserById(nonExistentUserId))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", nonExistentUserId))
                .andExpect(status().isInternalServerError());

        verify(userService).getUserById(eq(nonExistentUserId));
    }

    /**
     * Test updating user with invalid data
     * Verifies that validation errors are handled during updates
     */
    @Test
    @DisplayName("Should return validation error for invalid user update data")
    void testUpdateUser_InvalidData() throws Exception {
        // Given
        Long userId = 1L;
        UserDTO invalidUser = new UserDTO();
        // Missing required fields or invalid data

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test creating user with duplicate username
     * Verifies that duplicate username scenarios are handled
     */
    @Test
    @DisplayName("Should handle creating user with duplicate username")
    void testCreateUser_DuplicateUsername() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("existinguser");
        userDTO.setEmail("test@example.com");
        
        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isInternalServerError());

        verify(userService).createUser(any(UserDTO.class));
    }
}