package com.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for SecurityConfig.
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
     * Test passwordEncoder bean creation.
     * Verifies that the password encoder is properly configured as BCryptPasswordEncoder.
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
     * Test passwordEncoder functionality.
     * Verifies that the password encoder can encode passwords correctly.
     */
    @Test
    void testPasswordEncoder_ShouldEncodePassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "Password encoder should match raw password with encoded password");
    }

    /**
     * Test passwordEncoder with empty password.
     * Verifies that empty passwords are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithEmptyPassword_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // Act
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Assert
        assertNotNull(encodedPassword, "Encoded password should not be null even for empty input");
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword),
                "Password encoder should match empty password");
    }

    /**
     * Test passwordEncoder with special characters.
     * Verifies that passwords with special characters are encoded correctly.
     */
    @Test
    void testPasswordEncoder_WithSpecialCharacters_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "P@ssw0rd!#$%^&*()";

        // Act
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Assert
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword),
                "Password encoder should handle special characters");
    }

    /**
     * Test passwordEncoder produces different hashes for same password.
     * Verifies that BCrypt produces different hashes due to salt.
     */
    @Test
    void testPasswordEncoder_SamePassword_ShouldProduceDifferentHashes() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "testPassword123";

        // Act
        String encodedPassword1 = passwordEncoder.encode(password);
        String encodedPassword2 = passwordEncoder.encode(password);

        // Assert
        assertNotEquals(encodedPassword1, encodedPassword2,
                "BCrypt should produce different hashes for same password due to salt");
        assertTrue(passwordEncoder.matches(password, encodedPassword1),
                "First encoded password should match");
        assertTrue(passwordEncoder.matches(password, encodedPassword2),
                "Second encoded password should match");
    }

    /**
     * Test corsConfigurationSource bean creation.
     * Verifies that CORS configuration source is properly created.
     */
    @Test
    void testCorsConfigurationSource_ShouldReturnValidConfiguration() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsConfigurationSource, "CORS configuration source should not be null");
    }

    /**
     * Test corsConfigurationSource configuration.
     * Verifies that CORS configuration has correct settings.
     */
    @Test
    void testCorsConfigurationSource_ShouldHaveCorrectConfiguration() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration corsConfig = 
            corsConfigurationSource.getCorsConfiguration(null);

        // Assert
        assertNotNull(corsConfig, "CORS configuration should not be null");
        assertNotNull(corsConfig.getAllowedOrigins(), "Allowed origins should be configured");
        assertTrue(corsConfig.getAllowedOrigins().contains("*"),
                "CORS should allow all origins");
        assertNotNull(corsConfig.getAllowedMethods(), "Allowed methods should be configured");
        assertTrue(corsConfig.getAllowedMethods().contains("GET"),
                "CORS should allow GET method");
        assertTrue(corsConfig.getAllowedMethods().contains("POST"),
                "CORS should allow POST method");
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"),
                "CORS should allow PUT method");
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"),
                "CORS should allow DELETE method");
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"),
                "CORS should allow OPTIONS method");
    }

    /**
     * Test securityFilterChain bean creation.
     * Verifies that security filter chain is properly configured.
     *
     * @throws Exception if configuration fails
     */
    @Test
    void testSecurityFilterChain_ShouldReturnValidFilterChain() throws Exception {
        // Arrange
        HttpSecurity httpSecurity = new org.springframework.security.config.annotation.web.builders.HttpSecurity(
            new org.springframework.security.authentication.AuthenticationManagerBuilder(
                new org.springframework.security.config.annotation.ObjectPostProcessor<Object>() {
                    @Override
                    public <O> O postProcess(O object) {
                        return object;
                    }
                }
            ),
            new org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer(
                new org.springframework.security.config.annotation.web.builders.WebSecurity(
                    new org.springframework.security.config.annotation.ObjectPostProcessor<Object>() {
                        @Override
                        public <O> O postProcess(O object) {
                            return object;
                        }
                    }
                )
            ),
            new org.springframework.security.config.annotation.ObjectPostProcessor<Object>() {
                @Override
                public <O> O postProcess(O object) {
                    return object;
                }
            }
        );

        // Act
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);

        // Assert
        assertNotNull(filterChain, "Security filter chain should not be null");
    }

    /**
     * Test passwordEncoder with null password.
     * Verifies that null password handling.
     */
    @Test
    void testPasswordEncoder_WithNullPassword_ShouldThrowException() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> passwordEncoder.encode(null),
                "Encoding null password should throw IllegalArgumentException");
    }

    /**
     * Test passwordEncoder with very long password.
     * Verifies that long passwords are handled correctly.
     */
    @Test
    void testPasswordEncoder_WithVeryLongPassword_ShouldEncode() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(1000);

        // Act
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Assert
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword),
                "Password encoder should handle very long passwords");
    }

    /**
     * Test passwordEncoder matches with wrong password.
     * Verifies that wrong passwords don't match.
     */
    @Test
    void testPasswordEncoder_WithWrongPassword_ShouldNotMatch() {
        // Arrange
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // Act
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(matches, "Wrong password should not match encoded password");
    }
}