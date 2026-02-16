package com.ecommerce.security;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for UserDetailsServiceImpl
 * Tests user details loading functionality for Spring Security authentication
 */
@ExtendWith(MockitoExtension.class)
class test_UserDetailsServiceImpl {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);
    }

    /**
     * Test loading user by username with valid active user
     * Verifies that UserDetails is correctly created for valid user
     */
    @Test
    void testLoadUserByUsername_WithValidActiveUser_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals("testuser", userDetails.getUsername(), "Username should match");
        assertEquals("$2a$10$hashedPassword", userDetails.getPassword(), "Password hash should match");
        assertNotNull(userDetails.getAuthorities(), "Authorities should not be null");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user by username when user does not exist
     * Verifies that UsernameNotFoundException is thrown for non-existent user
     */
    @Test
    void testLoadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("nonexistent"))
            .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent"),
            "Should throw UsernameNotFoundException for non-existent user"
        );
        
        assertTrue(exception.getMessage().contains("User not found"),
                   "Exception message should indicate user not found");
        assertTrue(exception.getMessage().contains("nonexistent"),
                   "Exception message should contain username");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("nonexistent");
    }

    /**
     * Test loading user by username when user is inactive
     * Verifies that inactive users cannot be loaded (repository returns empty)
     */
    @Test
    void testLoadUserByUsername_WithInactiveUser_ShouldThrowException() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("testuser"),
            "Should throw UsernameNotFoundException for inactive user"
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user by username with null username
     * Verifies that null username is handled properly
     */
    @Test
    void testLoadUserByUsername_WithNullUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(null))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(null),
            "Should throw UsernameNotFoundException for null username"
        );
    }

    /**
     * Test loading user by username with empty username
     * Verifies that empty username is handled properly
     */
    @Test
    void testLoadUserByUsername_WithEmptyUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(""))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(""),
            "Should throw UsernameNotFoundException for empty username"
        );
    }

    /**
     * Test loading user by username with whitespace username
     * Verifies that whitespace-only username is handled properly
     */
    @Test
    void testLoadUserByUsername_WithWhitespaceUsername_ShouldThrowException() {
        // Arrange
        String whitespaceUsername = "   ";
        when(userRepository.findByUsernameAndIsActiveTrue(whitespaceUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(whitespaceUsername),
            "Should throw UsernameNotFoundException for whitespace username"
        );
    }

    /**
     * Test loading user by username when repository throws exception
     * Verifies that repository exceptions are propagated
     */
    @Test
    void testLoadUserByUsername_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(anyString()))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername("testuser"),
            "Should propagate repository exception"
        );
    }

    /**
     * Test loading user by username with case-sensitive username
     * Verifies that username lookup is case-sensitive
     */
    @Test
    void testLoadUserByUsername_WithDifferentCase_ShouldNotFindUser() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameAndIsActiveTrue("TESTUSER"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("TESTUSER"),
            "Should not find user with different case"
        );
        verify(userRepository, never()).findByUsernameAndIsActiveTrue("testuser");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("TESTUSER");
    }

    /**
     * Test UserDetails authorities are empty
     * Verifies that loaded user has empty authorities list
     */
    @Test
    void testLoadUserByUsername_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails.getAuthorities(), "Authorities should not be null");
        assertTrue(userDetails.getAuthorities().isEmpty(), "Authorities should be empty");
    }

    /**
     * Test loading user by username multiple times
     * Verifies that service can be called multiple times successfully
     */
    @Test
    void testLoadUserByUsername_MultipleTimes_ShouldSucceed() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails1 = userDetailsService.loadUserByUsername("testuser");
        UserDetails userDetails2 = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails1, "First UserDetails should not be null");
        assertNotNull(userDetails2, "Second UserDetails should not be null");
        assertEquals(userDetails1.getUsername(), userDetails2.getUsername(),
                     "Both UserDetails should have same username");
        verify(userRepository, times(2)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading different users
     * Verifies that service can load different users correctly
     */
    @Test
    void testLoadUserByUsername_WithDifferentUsers_ShouldReturnDifferentUserDetails() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");
        anotherUser.setPasswordHash("$2a$10$anotherHashedPassword");
        anotherUser.setEmail("another@example.com");
        anotherUser.setIsActive(true);

        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameAndIsActiveTrue("anotheruser"))
            .thenReturn(Optional.of(anotherUser));

        // Act
        UserDetails userDetails1 = userDetailsService.loadUserByUsername("testuser");
        UserDetails userDetails2 = userDetailsService.loadUserByUsername("anotheruser");

        // Assert
        assertNotEquals(userDetails1.getUsername(), userDetails2.getUsername(),
                        "Different users should have different usernames");
        assertNotEquals(userDetails1.getPassword(), userDetails2.getPassword(),
                        "Different users should have different passwords");
    }
}