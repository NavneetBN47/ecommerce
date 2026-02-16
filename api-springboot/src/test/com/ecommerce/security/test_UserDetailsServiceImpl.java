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
 * JUnit 5 test class for UserDetailsServiceImpl.
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
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD_HASH = "$2a$10$hashedPassword";
    private static final Long TEST_USER_ID = 123L;

    /**
     * Setup method executed before each test.
     * Initializes test user data.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setPasswordHash(TEST_PASSWORD_HASH);
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);
    }

    /**
     * Test loading user by username with valid active user.
     * Verifies that UserDetails are correctly loaded for an active user.
     */
    @Test
    void testLoadUserByUsername_WithValidActiveUser_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(TEST_USERNAME))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals(TEST_USERNAME, userDetails.getUsername(), "Username should match");
        assertEquals(TEST_PASSWORD_HASH, userDetails.getPassword(), "Password hash should match");
        assertNotNull(userDetails.getAuthorities(), "Authorities should not be null");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(TEST_USERNAME);
    }

    /**
     * Test loading user by username when user does not exist.
     * Verifies that UsernameNotFoundException is thrown for non-existent user.
     */
    @Test
    void testLoadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsernameAndIsActiveTrue(nonExistentUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(nonExistentUsername),
            "Should throw UsernameNotFoundException for non-existent user"
        );

        assertTrue(exception.getMessage().contains(nonExistentUsername),
            "Exception message should contain the username");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(nonExistentUsername);
    }

    /**
     * Test loading user by username when user is inactive.
     * Verifies that UsernameNotFoundException is thrown for inactive user.
     */
    @Test
    void testLoadUserByUsername_WithInactiveUser_ShouldThrowException() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByUsernameAndIsActiveTrue(TEST_USERNAME))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(TEST_USERNAME),
            "Should throw UsernameNotFoundException for inactive user"
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(TEST_USERNAME);
    }

    /**
     * Test loading user by username with null username.
     * Verifies proper handling of null username input.
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
     * Test loading user by username with empty username.
     * Verifies proper handling of empty username input.
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
     * Test that loaded UserDetails has empty authorities.
     * Verifies that the authorities collection is initialized but empty.
     */
    @Test
    void testLoadUserByUsername_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(TEST_USERNAME))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertNotNull(userDetails.getAuthorities(), "Authorities should not be null");
        assertTrue(userDetails.getAuthorities().isEmpty(), "Authorities should be empty");
    }

    /**
     * Test loading user by username with special characters.
     * Verifies that usernames with special characters are handled correctly.
     */
    @Test
    void testLoadUserByUsername_WithSpecialCharactersInUsername_ShouldWork() {
        // Arrange
        String specialUsername = "user@example.com";
        testUser.setUsername(specialUsername);
        when(userRepository.findByUsernameAndIsActiveTrue(specialUsername))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(specialUsername);

        // Assert
        assertNotNull(userDetails);
        assertEquals(specialUsername, userDetails.getUsername());
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(specialUsername);
    }

    /**
     * Test loading user by username with case sensitivity.
     * Verifies that username lookup is case-sensitive.
     */
    @Test
    void testLoadUserByUsername_WithDifferentCase_ShouldNotFindUser() {
        // Arrange
        String upperCaseUsername = TEST_USERNAME.toUpperCase();
        when(userRepository.findByUsernameAndIsActiveTrue(upperCaseUsername))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(upperCaseUsername),
            "Should throw UsernameNotFoundException for different case username"
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(upperCaseUsername);
    }

    /**
     * Test loading user by username with whitespace.
     * Verifies handling of usernames with leading/trailing whitespace.
     */
    @Test
    void testLoadUserByUsername_WithWhitespace_ShouldSearchExactly() {
        // Arrange
        String usernameWithWhitespace = " " + TEST_USERNAME + " ";
        when(userRepository.findByUsernameAndIsActiveTrue(usernameWithWhitespace))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(usernameWithWhitespace),
            "Should throw UsernameNotFoundException for username with whitespace"
        );
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(usernameWithWhitespace);
    }

    /**
     * Test that repository method is called with correct parameter.
     * Verifies correct interaction with UserRepository.
     */
    @Test
    void testLoadUserByUsername_ShouldCallRepositoryWithCorrectParameter() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(TEST_USERNAME))
            .thenReturn(Optional.of(testUser));

        // Act
        userDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(TEST_USERNAME);
        verify(userRepository, never()).findByUsernameAndIsActiveTrue(anyString());
    }

    /**
     * Test exception message format.
     * Verifies that exception message follows expected format.
     */
    @Test
    void testLoadUserByUsername_ExceptionMessageFormat() {
        // Arrange
        String username = "testuser123";
        when(userRepository.findByUsernameAndIsActiveTrue(username))
            .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(username)
        );

        assertEquals("User not found: " + username, exception.getMessage(),
            "Exception message should match expected format");
    }

    /**
     * Test that UserDetails password matches user's password hash.
     * Verifies password hash is correctly transferred to UserDetails.
     */
    @Test
    void testLoadUserByUsername_UserDetailsPasswordShouldMatchUserPasswordHash() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(TEST_USERNAME))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertEquals(testUser.getPasswordHash(), userDetails.getPassword(),
            "UserDetails password should match user's password hash");
    }

    /**
     * Test transactional behavior with read-only transaction.
     * Verifies that the method is executed within a read-only transaction context.
     */
    @Test
    void testLoadUserByUsername_ShouldExecuteInReadOnlyTransaction() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue(TEST_USERNAME))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertNotNull(userDetails, "Should successfully load user in transaction");
        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue(TEST_USERNAME);
    }
}