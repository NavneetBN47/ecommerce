package com.ecommerce.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SecurityConfig
 * 
 * Tests security configuration including password encoding and security filter chain
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
     * 
     * Verifies:
     * - Password encoder is not null
     * - Password encoder is instance of BCryptPasswordEncoder
     * - Password encoding works correctly
     */
    @Test
    void testPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder, "Password encoder should not be null");
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder, 
            "Password encoder should be BCryptPasswordEncoder");
        
        // Test encoding functionality
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), 
            "Password encoder should match raw and encoded passwords");
    }

    /**
     * Test that password encoder produces different hashes for same password
     * 
     * Verifies:
     * - BCrypt generates unique salt for each encoding
     * - Same password produces different hashes
     */
    @Test
    void testPasswordEncoderGeneratesUniqueSalts() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "samePassword";

        // When
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Then
        assertNotEquals(hash1, hash2, "BCrypt should generate different hashes for same password");
        assertTrue(passwordEncoder.matches(password, hash1), "First hash should match password");
        assertTrue(passwordEncoder.matches(password, hash2), "Second hash should match password");
    }

    /**
     * Test that password encoder rejects incorrect passwords
     * 
     * Verifies:
     * - Password encoder correctly identifies non-matching passwords
     */
    @Test
    void testPasswordEncoderRejectsIncorrectPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // When/Then
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword), 
            "Password encoder should reject incorrect password");
    }
}
