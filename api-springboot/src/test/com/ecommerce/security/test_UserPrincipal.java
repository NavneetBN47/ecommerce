package com.ecommerce.security;

import com.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for UserPrincipal
 * 
 * Tests user principal creation and methods including:
 * - Creating UserPrincipal from User entity
 * - Verifying user details
 * - Checking account status methods
 * - Validating authorities
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@DisplayName("UserPrincipal Tests")
class test_UserPrincipal {

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
     * Test creating UserPrincipal from User entity
     * 
     * Validates:
     * - UserPrincipal is created successfully
     * - All user details are mapped correctly
     * - Authorities are set
     */
    @Test
    @DisplayName("Should create UserPrincipal from User entity")
    void testCreate_Success() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal).isNotNull();
        assertThat(userPrincipal.getId()).isEqualTo(testUserId);
        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
        assertThat(userPrincipal.getPassword()).isEqualTo("encodedPassword");
        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
    }

    /**
     * Test UserPrincipal authorities
     * 
     * Validates:
     * - Authorities collection is not null
     * - Default ROLE_USER authority is present
     */
    @Test
    @DisplayName("Should have ROLE_USER authority")
    void testGetAuthorities() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        assertThat(authorities).isNotNull();
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    /**
     * Test account status methods
     * 
     * Validates:
     * - isAccountNonExpired returns true
     * - isAccountNonLocked returns true
     * - isCredentialsNonExpired returns true
     * - isEnabled returns true
     */
    @Test
    @DisplayName("Should have all account status flags as true")
    void testAccountStatusMethods() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
        assertThat(userPrincipal.isEnabled()).isTrue();
    }

    /**
     * Test getUsername method
     * 
     * Validates:
     * - Correct username is returned
     */
    @Test
    @DisplayName("Should return correct username")
    void testGetUsername() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
    }

    /**
     * Test getPassword method
     * 
     * Validates:
     * - Correct password is returned
     */
    @Test
    @DisplayName("Should return correct password")
    void testGetPassword() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getPassword()).isEqualTo("encodedPassword");
    }

    /**
     * Test getId method
     * 
     * Validates:
     * - Correct user ID is returned
     */
    @Test
    @DisplayName("Should return correct user ID")
    void testGetId() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getId()).isEqualTo(testUserId);
    }

    /**
     * Test getEmail method
     * 
     * Validates:
     * - Correct email is returned
     */
    @Test
    @DisplayName("Should return correct email")
    void testGetEmail() {
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
    }
}