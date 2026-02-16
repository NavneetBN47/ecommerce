package com.ecommerce.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for SecurityConfig
 * Tests security configuration beans and settings
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Test that passwordEncoder bean returns BCryptPasswordEncoder instance
     * Verifies proper password encoding configuration
     */
    @Test
    void passwordEncoderShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder, "PasswordEncoder should not be null");
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder, 
            "PasswordEncoder should be instance of BCryptPasswordEncoder");
    }

    /**
     * Test that passwordEncoder encodes passwords correctly
     * Verifies BCrypt encoding functionality
     */
    @Test
    void passwordEncoderShouldEncodePassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), 
            "Encoded password should match raw password");
    }

    /**
     * Test that passwordEncoder produces different hashes for same password
     * Verifies BCrypt salt functionality
     */
    @Test
    void passwordEncoderShouldProduceDifferentHashesForSamePassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword1 = passwordEncoder.encode(rawPassword);
        String encodedPassword2 = passwordEncoder.encode(rawPassword);

        // Then
        assertNotEquals(encodedPassword1, encodedPassword2, 
            "BCrypt should produce different hashes for same password due to salt");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1));
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2));
    }

    /**
     * Test that passwordEncoder rejects incorrect passwords
     * Verifies password matching validation
     */
    @Test
    void passwordEncoderShouldRejectIncorrectPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword), 
            "Wrong password should not match encoded password");
    }

    /**
     * Test that passwordEncoder handles empty passwords
     * Verifies edge case handling
     */
    @Test
    void passwordEncoderShouldHandleEmptyPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // When
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Then
        assertNotNull(encodedPassword, "Encoded password should not be null even for empty input");
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    /**
     * Test that passwordEncoder handles null password gracefully
     * Verifies null safety
     */
    @Test
    void passwordEncoderShouldHandleNullPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(null);
        }, "Encoding null password should throw IllegalArgumentException");
    }
}