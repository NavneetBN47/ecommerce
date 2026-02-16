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
 * Unit tests for UserDetailsServiceImpl.
 * Tests user details loading for Spring Security authentication.
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
     * Test loading user by username with valid active user.
     * Verifies that UserDetails is correctly created for active user.
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
        assertEquals("$2a$10$hashedPassword", userDetails.getPassword());
        assertNotNull(userDetails.getAuthorities());
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user by username when user does not exist.
     * Verifies that UsernameNotFoundException is thrown for non-existent user.
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
     * Test loading user by username when user is inactive.
     * Verifies that inactive users cannot be loaded.
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
            () -> userDetailsService.loadUserByUsername("testuser")
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user by username with null username.
     * Verifies that null username is handled appropriately.
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
     * Test loading user by username with empty username.
     * Verifies that empty username is handled appropriately.
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
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("");
    }

    /**
     * Test loading user by username with special characters.
     * Verifies that usernames with special characters are handled correctly.
     */
    @Test
    void testLoadUserByUsername_WithSpecialCharacters_ShouldReturnUserDetails() {
        // Arrange
        testUser.setUsername("test.user@example");
        when(userRepository.findByUsernameAndIsActiveTrue("test.user@example"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test.user@example");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test.user@example", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("test.user@example");
    }

    /**
     * Test loading user by username with case sensitivity.
     * Verifies that username lookup is case-sensitive.
     */
    @Test
    void testLoadUserByUsername_WithDifferentCase_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("TESTUSER"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("TESTUSER")
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("TESTUSER");
    }

    /**
     * Test that loaded UserDetails has empty authorities collection.
     * Verifies that authorities are initialized as empty list.
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
     * Test loading user by username when repository throws exception.
     * Verifies that repository exceptions are propagated.
     */
    @Test
    void testLoadUserByUsername_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(anyString()))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername("testuser")
        );
    }

    /**
     * Test loading user multiple times.
     * Verifies that service can handle multiple consecutive calls.
     */
    @Test
    void testLoadUserByUsername_MultipleTimes_ShouldWorkConsistently() {
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
     * Test that UserDetails contains correct password hash.
     * Verifies that password hash is correctly transferred to UserDetails.
     */
    @Test
    void testLoadUserByUsername_ShouldReturnCorrectPasswordHash() {
        // Arrange
        String expectedPasswordHash = "$2a$10$specificHashValue";
        testUser.setPasswordHash(expectedPasswordHash);
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertEquals(expectedPasswordHash, userDetails.getPassword());
    }
}