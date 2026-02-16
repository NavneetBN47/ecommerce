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
        testUser.setPasswordHash("$2a$10$encodedPasswordHash");
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);
    }

    /**
     * Test loading user by username with valid active user
     * Verifies that UserDetails is returned for existing active user
     */
    @Test
    void testLoadUserByUsername_WithValidActiveUser_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$encodedPasswordHash", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
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
            () -> userDetailsService.loadUserByUsername("nonexistent")
        );
        
        assertTrue(exception.getMessage().contains("User not found: nonexistent"));
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("nonexistent");
    }

    /**
     * Test loading user by username when user is inactive
     * Verifies that inactive users are not loaded
     */
    @Test
    void testLoadUserByUsername_WithInactiveUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("inactiveuser"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("inactiveuser")
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("inactiveuser");
    }

    /**
     * Test loading user by username with null username
     * Verifies that appropriate exception is thrown for null username
     */
    @Test
    void testLoadUserByUsername_WithNullUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(null))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(null)
        );
    }

    /**
     * Test loading user by username with empty username
     * Verifies that appropriate exception is thrown for empty username
     */
    @Test
    void testLoadUserByUsername_WithEmptyUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(""))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("")
        );
    }

    /**
     * Test that UserDetails has empty authorities collection
     * Verifies that loaded user has no granted authorities
     */
    @Test
    void testLoadUserByUsername_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    /**
     * Test loading user with special characters in username
     * Verifies that usernames with special characters are handled correctly
     */
    @Test
    void testLoadUserByUsername_WithSpecialCharactersInUsername_ShouldWork() {
        // Arrange
        String specialUsername = "test.user@domain";
        testUser.setUsername(specialUsername);
        when(userRepository.findByUsernameAndIsActiveTrue(specialUsername))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(specialUsername);

        // Assert
        assertNotNull(userDetails);
        assertEquals(specialUsername, userDetails.getUsername());
    }

    /**
     * Test that repository is called with exact username
     * Verifies that the service passes username to repository without modification
     */
    @Test
    void testLoadUserByUsername_ShouldCallRepositoryWithExactUsername() {
        // Arrange
        String username = "TestUser123";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenReturn(Optional.of(testUser));

        // Act
        userDetailsService.loadUserByUsername(username);

        // Assert
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(username);
        verify(userRepository, never()).findByUsernameAndIsActiveTrue(anyString());
    }

    /**
     * Test loading user multiple times
     * Verifies that service can load user details multiple times
     */
    @Test
    void testLoadUserByUsername_CalledMultipleTimes_ShouldWorkCorrectly() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails1 = userDetailsService.loadUserByUsername("testuser");
        UserDetails userDetails2 = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails1);
        assertNotNull(userDetails2);
        assertEquals(userDetails1.getUsername(), userDetails2.getUsername());
        verify(userRepository, times(2)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test that UserDetails password matches User passwordHash
     * Verifies that the password from User entity is correctly mapped to UserDetails
     */
    @Test
    void testLoadUserByUsername_ShouldMapPasswordCorrectly() {
        // Arrange
        String expectedPasswordHash = "$2a$10$testHashValue";
        testUser.setPasswordHash(expectedPasswordHash);
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertEquals(expectedPasswordHash, userDetails.getPassword());
    }
}