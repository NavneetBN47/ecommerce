package com.ecommerce.security;

import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Test class for CustomUserDetailsService
 * 
 * Tests user details loading for authentication including:
 * - Loading user by username
 * - Loading user by ID
 * - Handling non-existent users
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class test_CustomUserDetailsService {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private UUID testUserId;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
            .id(testUserId)
            .username("testuser")
            .password("encodedPassword")
            .fullName("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .emailVerified(false)
            .build();
    }

    /**
     * Test loading user by username successfully
     * 
     * Validates:
     * - UserDetails is returned
     * - Correct username and password
     * - User authorities are set
     */
    @Test
    @DisplayName("Should load user by username successfully")
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).isNotEmpty();
    }

    /**
     * Test loading user by non-existent username
     * 
     * Validates:
     * - UsernameNotFoundException is thrown
     * - Appropriate error message
     */
    @Test
    @DisplayName("Should throw exception when username not found")
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    /**
     * Test loading user by ID successfully
     * 
     * Validates:
     * - UserDetails is returned
     * - Correct user ID
     * - User details match
     */
    @Test
    @DisplayName("Should load user by ID successfully")
    void testLoadUserById_Success() {
        when(userRepository.findById(testUserId))
            .thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserById(testUserId);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(((UserPrincipal) userDetails).getId()).isEqualTo(testUserId);
    }

    /**
     * Test loading user by non-existent ID
     * 
     * Validates:
     * - ResourceNotFoundException is thrown
     * - Appropriate error message
     */
    @Test
    @DisplayName("Should throw exception when user ID not found")
    void testLoadUserById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById(nonExistentId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }
}