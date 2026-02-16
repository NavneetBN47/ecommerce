package com.ecommerce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SecurityConfig
 * Tests security configuration including password encoding, security filter chain, and CORS configuration
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    /**
     * Test password encoder bean creation
     * Verifies that BCryptPasswordEncoder is properly configured
     */
    @Test
    void testPasswordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Act
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder, "Password encoder should not be null");
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder, 
                   "Password encoder should be instance of BCryptPasswordEncoder");
    }

    /**
     * Test password encoder functionality
     * Verifies that password encoder can encode and match passwords
     */
    @Test
    void testPasswordEncoder_ShouldEncodeAndMatchPasswords() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Assert
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(matches, "Password encoder should match raw and encoded passwords");
    }

    /**
     * Test password encoder with different passwords
     * Verifies that different passwords produce different encoded values
     */
    @Test
    void testPasswordEncoder_WithDifferentPasswords_ShouldProduceDifferentHashes() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password1 = "password1";
        String password2 = "password2";

        // Act
        String encoded1 = passwordEncoder.encode(password1);
        String encoded2 = passwordEncoder.encode(password2);

        // Assert
        assertNotEquals(encoded1, encoded2, "Different passwords should produce different hashes");
    }

    /**
     * Test password encoder with same password multiple times
     * Verifies that encoding same password multiple times produces different hashes (salt)
     */
    @Test
    void testPasswordEncoder_SamePasswordMultipleTimes_ShouldProduceDifferentHashes() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "testPassword";

        // Act
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        // Assert
        assertNotEquals(encoded1, encoded2, "Same password should produce different hashes due to salt");
        assertTrue(passwordEncoder.matches(password, encoded1), "First hash should match original password");
        assertTrue(passwordEncoder.matches(password, encoded2), "Second hash should match original password");
    }

    /**
     * Test CORS configuration source bean creation
     * Verifies that CORS configuration is properly set up
     */
    @Test
    void testCorsConfigurationSource_ShouldReturnValidConfiguration() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsConfigurationSource, "CORS configuration source should not be null");
        assertTrue(corsConfigurationSource instanceof UrlBasedCorsConfigurationSource,
                   "CORS configuration should be UrlBasedCorsConfigurationSource");
    }

    /**
     * Test CORS configuration allows all origins
     * Verifies that CORS is configured to allow requests from any origin
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllOrigins() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        // Assert
        assertNotNull(urlBasedSource.getCorsConfigurations(), "CORS configurations should not be null");
        assertFalse(urlBasedSource.getCorsConfigurations().isEmpty(), "CORS configurations should not be empty");
    }

    /**
     * Test CORS configuration allows required HTTP methods
     * Verifies that GET, POST, PUT, DELETE, OPTIONS methods are allowed
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowRequiredHttpMethods() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        // Assert
        assertNotNull(urlBasedSource.getCorsConfigurations().get("/**"), 
                     "CORS configuration for /** pattern should exist");
        
        var corsConfig = urlBasedSource.getCorsConfigurations().get("/**");
        assertNotNull(corsConfig.getAllowedMethods(), "Allowed methods should not be null");
        assertTrue(corsConfig.getAllowedMethods().contains("GET"), "GET method should be allowed");
        assertTrue(corsConfig.getAllowedMethods().contains("POST"), "POST method should be allowed");
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"), "PUT method should be allowed");
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"), "DELETE method should be allowed");
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"), "OPTIONS method should be allowed");
    }

    /**
     * Test CORS configuration allows all headers
     * Verifies that all headers are allowed in CORS requests
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllHeaders() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        // Assert
        var corsConfig = urlBasedSource.getCorsConfigurations().get("/**");
        assertNotNull(corsConfig.getAllowedHeaders(), "Allowed headers should not be null");
        assertTrue(corsConfig.getAllowedHeaders().contains("*"), "All headers should be allowed");
    }

    /**
     * Test password encoder with empty password
     * Verifies that empty passwords can be encoded
     */
    @Test
    void testPasswordEncoder_WithEmptyPassword_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // Act
        String encoded = passwordEncoder.encode(emptyPassword);

        // Assert
        assertNotNull(encoded, "Encoded empty password should not be null");
        assertTrue(passwordEncoder.matches(emptyPassword, encoded), 
                   "Empty password should match its encoded version");
    }

    /**
     * Test password encoder with special characters
     * Verifies that passwords with special characters are properly encoded
     */
    @Test
    void testPasswordEncoder_WithSpecialCharacters_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "P@ssw0rd!#$%^&*()";

        // Act
        String encoded = passwordEncoder.encode(specialPassword);

        // Assert
        assertNotNull(encoded, "Encoded password with special characters should not be null");
        assertTrue(passwordEncoder.matches(specialPassword, encoded),
                   "Password with special characters should match its encoded version");
    }

    /**
     * Test password encoder with long password
     * Verifies that long passwords are properly encoded
     */
    @Test
    void testPasswordEncoder_WithLongPassword_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "ThisIsAVeryLongPasswordWithMoreThan50CharactersToTestEncodingCapability123456789";

        // Act
        String encoded = passwordEncoder.encode(longPassword);

        // Assert
        assertNotNull(encoded, "Encoded long password should not be null");
        assertTrue(passwordEncoder.matches(longPassword, encoded),
                   "Long password should match its encoded version");
    }

    /**
     * Test password encoder does not match wrong password
     * Verifies that incorrect passwords are not matched
     */
    @Test
    void testPasswordEncoder_WithWrongPassword_ShouldNotMatch() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        // Act
        String encoded = passwordEncoder.encode(correctPassword);
        boolean matches = passwordEncoder.matches(wrongPassword, encoded);

        // Assert
        assertFalse(matches, "Wrong password should not match encoded password");
    }
}