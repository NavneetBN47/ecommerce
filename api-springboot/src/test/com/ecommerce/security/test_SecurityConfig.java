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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for SecurityConfig.
 * Tests Spring Security configuration including password encoding,
 * security filter chain, and CORS configuration.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Setup method executed before each test.
     * Initializes test environment.
     */
    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    /**
     * Test password encoder bean creation.
     * Verifies that a BCryptPasswordEncoder is created.
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
     * Test password encoder functionality.
     * Verifies that the password encoder can encode and match passwords.
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
     * Test password encoder with different passwords.
     * Verifies that different passwords produce different hashes.
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
     * Test password encoder with same password multiple times.
     * Verifies that encoding the same password produces different hashes (salt).
     */
    @Test
    void testPasswordEncoder_WithSamePassword_ShouldProduceDifferentHashesDueToSalt() {
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
     * Test CORS configuration source bean creation.
     * Verifies that CORS configuration source is created properly.
     */
    @Test
    void testCorsConfigurationSource_ShouldReturnValidConfiguration() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsConfigurationSource, "CORS configuration source should not be null");
        assertTrue(corsConfigurationSource instanceof UrlBasedCorsConfigurationSource,
            "CORS configuration source should be UrlBasedCorsConfigurationSource");
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
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(corsConfig, "CORS configuration should not be null");
        assertTrue(corsConfig.getAllowedOrigins().contains("*"),
            "CORS should allow all origins");
    }

    /**
     * Test CORS configuration allows required HTTP methods.
     * Verifies that all necessary HTTP methods are allowed.
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowRequiredHttpMethods() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(corsConfig, "CORS configuration should not be null");
        assertTrue(corsConfig.getAllowedMethods().containsAll(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")),
            "CORS should allow GET, POST, PUT, DELETE, and OPTIONS methods");
    }

    /**
     * Test CORS configuration allows all headers.
     * Verifies that all headers are allowed in CORS configuration.
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllHeaders() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(corsConfig, "CORS configuration should not be null");
        assertTrue(corsConfig.getAllowedHeaders().contains("*"),
            "CORS should allow all headers");
    }

    /**
     * Test CORS configuration is registered for all paths.
     * Verifies that CORS configuration applies to all endpoints.
     */
    @Test
    void testCorsConfigurationSource_ShouldBeRegisteredForAllPaths() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;

        // Assert
        assertTrue(urlBasedSource.getCorsConfigurations().containsKey("/**"),
            "CORS configuration should be registered for all paths (/**)");
    }

    /**
     * Test password encoder with empty password.
     * Verifies that empty passwords can be encoded.
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
     * Test password encoder with special characters.
     * Verifies that passwords with special characters are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithSpecialCharacters_ShouldEncodeCorrectly() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "p@ssw0rd!#$%^&*()";

        // Act
        String encoded = passwordEncoder.encode(specialPassword);
        boolean matches = passwordEncoder.matches(specialPassword, encoded);

        // Assert
        assertNotNull(encoded);
        assertTrue(matches, "Password with special characters should match its encoded version");
    }

    /**
     * Test password encoder with long password.
     * Verifies that long passwords are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithLongPassword_ShouldEncodeCorrectly() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(100);

        // Act
        String encoded = passwordEncoder.encode(longPassword);
        boolean matches = passwordEncoder.matches(longPassword, encoded);

        // Assert
        assertNotNull(encoded);
        assertTrue(matches, "Long password should match its encoded version");
    }

    /**
     * Test password encoder with unicode characters.
     * Verifies that passwords with unicode characters are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithUnicodeCharacters_ShouldEncodeCorrectly() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String unicodePassword = "пароль密码";

        // Act
        String encoded = passwordEncoder.encode(unicodePassword);
        boolean matches = passwordEncoder.matches(unicodePassword, encoded);

        // Assert
        assertNotNull(encoded);
        assertTrue(matches, "Password with unicode characters should match its encoded version");
    }

    /**
     * Test password encoder does not match wrong password.
     * Verifies that wrong passwords are rejected.
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