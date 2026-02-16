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
 * Test class for UserDetailsServiceImpl.
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
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);
    }

    /**
     * Test successful user loading by username.
     * Verifies that UserDetails is returned with correct information.
     */
    @Test
    void testLoadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$hashedPassword", userDetails.getPassword());
        assertNotNull(userDetails.getAuthorities());
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user with non-existent username.
     * Verifies that UsernameNotFoundException is thrown.
     */
    @Test
    void testLoadUserByUsername_WithNonExistentUsername_ShouldThrowException() {
        when(userRepository.findByUsernameAndIsActiveTrue("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found: nonexistent"));
        verify(userRepository).findByUsernameAndIsActiveTrue("nonexistent");
    }

    /**
     * Test loading user with null username.
     * Verifies that appropriate exception is thrown.
     */
    @Test
    void testLoadUserByUsername_WithNullUsername_ShouldThrowException() {
        when(userRepository.findByUsernameAndIsActiveTrue(null)).thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(null)
        );

        verify(userRepository).findByUsernameAndIsActiveTrue(null);
    }

    /**
     * Test loading user with empty username.
     * Verifies that UsernameNotFoundException is thrown.
     */
    @Test
    void testLoadUserByUsername_WithEmptyUsername_ShouldThrowException() {
        when(userRepository.findByUsernameAndIsActiveTrue("")).thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("")
        );

        verify(userRepository).findByUsernameAndIsActiveTrue("");
    }

    /**
     * Test that only active users are loaded.
     * Verifies that inactive users are not returned.
     */
    @Test
    void testLoadUserByUsername_WithInactiveUser_ShouldThrowException() {
        testUser.setIsActive(false);
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("testuser")
        );

        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user with special characters in username.
     * Verifies that special characters are handled correctly.
     */
    @Test
    void testLoadUserByUsername_WithSpecialCharacters_ShouldReturnUserDetails() {
        String specialUsername = "test.user@domain";
        testUser.setUsername(specialUsername);
        when(userRepository.findByUsernameAndIsActiveTrue(specialUsername)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(specialUsername);

        assertNotNull(userDetails);
        assertEquals(specialUsername, userDetails.getUsername());
        verify(userRepository).findByUsernameAndIsActiveTrue(specialUsername);
    }

    /**
     * Test that UserDetails has empty authorities collection.
     * Verifies default authorities setup.
     */
    @Test
    void testLoadUserByUsername_ShouldReturnUserDetailsWithEmptyAuthorities() {
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    /**
     * Test that password hash is correctly transferred to UserDetails.
     * Verifies password field mapping.
     */
    @Test
    void testLoadUserByUsername_ShouldTransferPasswordHash() {
        String passwordHash = "$2a$10$specificHashValue";
        testUser.setPasswordHash(passwordHash);
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertEquals(passwordHash, userDetails.getPassword());
    }

    /**
     * Test loading user with case-sensitive username.
     * Verifies that username matching is case-sensitive.
     */
    @Test
    void testLoadUserByUsername_WithDifferentCase_ShouldCallRepositoryWithExactCase() {
        String upperCaseUsername = "TESTUSER";
        when(userRepository.findByUsernameAndIsActiveTrue(upperCaseUsername)).thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(upperCaseUsername)
        );

        verify(userRepository).findByUsernameAndIsActiveTrue(upperCaseUsername);
        verify(userRepository, never()).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test that repository is called exactly once per load request.
     * Verifies no caching or multiple calls occur.
     */
    @Test
    void testLoadUserByUsername_ShouldCallRepositoryOnce() {
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(testUser));

        userDetailsService.loadUserByUsername("testuser");

        verify(userRepository, times(1)).findByUsernameAndIsActiveTrue("testuser");
    }

    /**
     * Test loading user when repository throws exception.
     * Verifies that exceptions are propagated correctly.
     */
    @Test
    void testLoadUserByUsername_WhenRepositoryThrowsException_ShouldPropagateException() {
        when(userRepository.findByUsernameAndIsActiveTrue(anyString()))
            .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername("testuser")
        );
    }

    /**
     * Test that UserDetails is instance of Spring Security User class.
     * Verifies correct UserDetails implementation type.
     */
    @Test
    void testLoadUserByUsername_ShouldReturnSpringSecurityUserInstance() {
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertTrue(userDetails instanceof org.springframework.security.core.userdetails.User);
    }
}