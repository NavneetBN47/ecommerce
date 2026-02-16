package com.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SecurityConfig
 * Tests security configuration including password encoding, CORS, and security filter chain
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Test password encoder bean creation
     * Verifies that a BCryptPasswordEncoder is created
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
     * Test password encoder functionality
     * Verifies that the password encoder can encode and match passwords
     */
    @Test
    void testPasswordEncoder_ShouldEncodeAndMatchPasswords() {
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
     * Test password encoder with different passwords
     * Verifies that same password generates different hashes
     */
    @Test
    void testPasswordEncoder_SamePasswordShouldGenerateDifferentHashes() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "testPassword123";

        // Act
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Assert
        assertNotEquals(hash1, hash2);
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }

    /**
     * Test password encoder with wrong password
     * Verifies that wrong password does not match
     */
    @Test
    void testPasswordEncoder_WrongPasswordShouldNotMatch() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // Act
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(matches);
    }

    /**
     * Test CORS configuration source creation
     * Verifies that CORS configuration source is created properly
     */
    @Test
    void testCorsConfigurationSource_ShouldReturnValidConfiguration() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsConfigurationSource);
    }

    /**
     * Test CORS configuration allows all origins
     * Verifies that CORS is configured to allow all origins
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllOrigins() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        var corsConfig = corsConfigurationSource.getCorsConfiguration("/**");

        // Assert
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("*"));
    }

    /**
     * Test CORS configuration allows required HTTP methods
     * Verifies that all standard HTTP methods are allowed
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowRequiredMethods() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        var corsConfig = corsConfigurationSource.getCorsConfiguration("/**");

        // Assert
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }

    /**
     * Test CORS configuration allows all headers
     * Verifies that all headers are allowed in CORS configuration
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllHeaders() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        var corsConfig = corsConfigurationSource.getCorsConfiguration("/**");

        // Assert
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedHeaders().contains("*"));
    }

    /**
     * Test password encoder with empty password
     * Verifies that empty password can be encoded
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
     * Test password encoder with special characters
     * Verifies that passwords with special characters are handled correctly
     */
    @Test
    void testPasswordEncoder_WithSpecialCharacters_ShouldEncodeCorrectly() {
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
     * Test password encoder with very long password
     * Verifies that long passwords are handled correctly
     */
    @Test
    void testPasswordEncoder_WithLongPassword_ShouldEncodeCorrectly() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(100);

        // Act
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }
}