package com.ecommerce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityConfig.
 * Tests security configuration beans and settings.
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Test password encoder bean creation.
     * Verifies that BCryptPasswordEncoder is properly configured.
     */
    @Test
    void testPasswordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Act
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder);
        assertEquals("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder", 
                     passwordEncoder.getClass().getName());
    }

    /**
     * Test password encoder functionality.
     * Verifies that password encoding works correctly.
     */
    @Test
    void testPasswordEncoder_ShouldEncodePassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    /**
     * Test password encoder with different passwords.
     * Verifies that same password produces different hashes.
     */
    @Test
    void testPasswordEncoder_SamePasswordShouldProduceDifferentHashes() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword1 = passwordEncoder.encode(rawPassword);
        String encodedPassword2 = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword1);
        assertNotNull(encodedPassword2);
        assertNotEquals(encodedPassword1, encodedPassword2);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1));
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2));
    }

    /**
     * Test password encoder with wrong password.
     * Verifies that wrong password does not match.
     */
    @Test
    void testPasswordEncoder_WrongPasswordShouldNotMatch() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(matches);
    }

    /**
     * Test CORS configuration source bean creation.
     * Verifies that CORS configuration is properly set up.
     */
    @Test
    void testCorsConfigurationSource_ShouldReturnValidConfiguration() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsConfigurationSource);
        assertTrue(corsConfigurationSource instanceof UrlBasedCorsConfigurationSource);
    }

    /**
     * Test CORS configuration allows all origins.
     * Verifies that CORS is configured to allow all origins.
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllOrigins() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        // Assert
        assertNotNull(urlBasedSource);
        assertNotNull(urlBasedSource.getCorsConfigurations());
    }

    /**
     * Test password encoder with empty password.
     * Verifies that empty password can be encoded.
     */
    @Test
    void testPasswordEncoder_WithEmptyPassword_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // Act
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    /**
     * Test password encoder with special characters.
     * Verifies that passwords with special characters are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithSpecialCharacters_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "P@ssw0rd!#$%^&*()";

        // Act
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword));
    }

    /**
     * Test password encoder with very long password.
     * Verifies that long passwords are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithLongPassword_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(100);

        // Act
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }

    /**
     * Test password encoder with unicode characters.
     * Verifies that passwords with unicode characters are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithUnicodeCharacters_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String unicodePassword = "パスワード123";

        // Act
        String encodedPassword = passwordEncoder.encode(unicodePassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword));
    }

    /**
     * Test that password encoder is consistent.
     * Verifies that the same encoder instance can validate previously encoded passwords.
     */
    @Test
    void testPasswordEncoder_ShouldBeConsistent() {
        // Arrange
        PasswordEncoder passwordEncoder1 = securityConfig.passwordEncoder();
        PasswordEncoder passwordEncoder2 = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder1.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder2.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(matches);
    }
}