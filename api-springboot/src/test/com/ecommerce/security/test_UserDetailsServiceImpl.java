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
 * JUnit test class for UserDetailsServiceImpl.
 * Tests user details loading functionality for Spring Security authentication.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class test_UserDetailsServiceImpl {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    /**
     * Setup method to initialize test data before each test.
     */
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
     * Test loadUserByUsername with existing active user.
     * Verifies that UserDetails is correctly loaded for an existing active user.
     */
    @Test
    void testLoadUserByUsername_WithExistingActiveUser_ShouldReturnUserDetails() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals(testUser.getUsername(), userDetails.getUsername(),
                "Username should match");
        assertEquals(testUser.getPasswordHash(), userDetails.getPassword(),
                "Password hash should match");
        assertNotNull(userDetails.getAuthorities(), "Authorities should not be null");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(username);
    }

    /**
     * Test loadUserByUsername with non-existing user.
     * Verifies that UsernameNotFoundException is thrown for non-existing user.
     */
    @Test
    void testLoadUserByUsername_WithNonExistingUser_ShouldThrowException() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Should throw UsernameNotFoundException for non-existing user"
        );
        
        assertTrue(exception.getMessage().contains(username),
                "Exception message should contain the username");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(username);
    }

    /**
     * Test loadUserByUsername with inactive user.
     * Verifies that inactive users are not loaded (treated as non-existing).
     */
    @Test
    void testLoadUserByUsername_WithInactiveUser_ShouldThrowException() {
        // Arrange
        String username = "inactiveuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Should throw UsernameNotFoundException for inactive user"
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(username);
    }

    /**
     * Test loadUserByUsername with null username.
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
            () -> userDetailsService.loadUserByUsername(null),
            "Should throw UsernameNotFoundException for null username"
        );
    }

    /**
     * Test loadUserByUsername with empty username.
     * Verifies that empty username is handled appropriately.
     */
    @Test
    void testLoadUserByUsername_WithEmptyUsername_ShouldThrowException() {
        // Arrange
        String emptyUsername = "";
        when(userRepository.findByUsernameAndIsActiveTrue(emptyUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(emptyUsername),
            "Should throw UsernameNotFoundException for empty username"
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(emptyUsername);
    }

    /**
     * Test loadUserByUsername with whitespace username.
     * Verifies that whitespace-only username is handled appropriately.
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
     * Test loadUserByUsername with special characters in username.
     * Verifies that usernames with special characters are handled correctly.
     */
    @Test
    void testLoadUserByUsername_WithSpecialCharacters_ShouldReturnUserDetails() {
        // Arrange
        String specialUsername = "test@user.com";
        testUser.setUsername(specialUsername);
        when(userRepository.findByUsernameAndIsActiveTrue(specialUsername))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(specialUsername);

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals(specialUsername, userDetails.getUsername(),
                "Username with special characters should match");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(specialUsername);
    }

    /**
     * Test loadUserByUsername with case-sensitive username.
     * Verifies that username lookup is case-sensitive.
     */
    @Test
    void testLoadUserByUsername_CaseSensitive_ShouldBeDistinct() {
        // Arrange
        String lowercaseUsername = "testuser";
        String uppercaseUsername = "TESTUSER";
        when(userRepository.findByUsernameAndIsActiveTrue(lowercaseUsername))
            .thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameAndIsActiveTrue(uppercaseUsername))
            .thenReturn(Optional.empty());

        // Act
        UserDetails userDetailsLowercase = userDetailsService.loadUserByUsername(lowercaseUsername);
        
        // Assert
        assertNotNull(userDetailsLowercase, "UserDetails should be found for lowercase username");
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(uppercaseUsername),
            "Should throw exception for different case username"
        );
    }

    /**
     * Test loadUserByUsername returns correct authorities.
     * Verifies that the returned UserDetails has an empty authorities list.
     */
    @Test
    void testLoadUserByUsername_ShouldReturnEmptyAuthorities() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails.getAuthorities(), "Authorities should not be null");
        assertTrue(userDetails.getAuthorities().isEmpty(),
                "Authorities list should be empty");
    }

    /**
     * Test loadUserByUsername when repository throws exception.
     * Verifies that repository exceptions are propagated correctly.
     */
    @Test
    void testLoadUserByUsername_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Should propagate repository exception"
        );
    }

    /**
     * Test loadUserByUsername with very long username.
     * Verifies that long usernames are handled correctly.
     */
    @Test
    void testLoadUserByUsername_WithVeryLongUsername_ShouldHandleCorrectly() {
        // Arrange
        String longUsername = "a".repeat(255);
        testUser.setUsername(longUsername);
        when(userRepository.findByUsernameAndIsActiveTrue(longUsername))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(longUsername);

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null for long username");
        assertEquals(longUsername, userDetails.getUsername(),
                "Long username should match");
    }
}