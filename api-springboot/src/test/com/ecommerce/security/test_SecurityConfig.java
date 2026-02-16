package com.ecommerce.security;

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
 * Test class for SecurityConfig.
 * Tests security configuration beans and CORS settings.
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Test that passwordEncoder bean returns BCryptPasswordEncoder instance.
     * Verifies correct password encoder type.
     */
    @Test
    void testPasswordEncoder_ShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    /**
     * Test that passwordEncoder can encode passwords.
     * Verifies basic encoding functionality.
     */
    @Test
    void testPasswordEncoder_ShouldEncodePassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2"));
    }

    /**
     * Test that passwordEncoder can match encoded passwords.
     * Verifies password matching functionality.
     */
    @Test
    void testPasswordEncoder_ShouldMatchPasswords() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        assertTrue(matches);
    }

    /**
     * Test that passwordEncoder doesn't match wrong passwords.
     * Verifies password mismatch detection.
     */
    @Test
    void testPasswordEncoder_ShouldNotMatchWrongPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        assertFalse(matches);
    }

    /**
     * Test that corsConfigurationSource bean is created.
     * Verifies CORS configuration source creation.
     */
    @Test
    void testCorsConfigurationSource_ShouldReturnValidSource() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        assertNotNull(corsConfigurationSource);
        assertTrue(corsConfigurationSource instanceof UrlBasedCorsConfigurationSource);
    }

    /**
     * Test that CORS configuration allows all origins.
     * Verifies CORS allowed origins configuration.
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllOrigins() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        assertNotNull(urlBasedSource.getCorsConfigurations());
        assertTrue(urlBasedSource.getCorsConfigurations().containsKey("/**"));
    }

    /**
     * Test that CORS configuration includes expected HTTP methods.
     * Verifies allowed HTTP methods in CORS configuration.
     */
    @Test
    void testCorsConfigurationSource_ShouldIncludeExpectedMethods() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        var corsConfig = urlBasedSource.getCorsConfigurations().get("/**");
        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedMethods());
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }

    /**
     * Test that CORS configuration allows all headers.
     * Verifies allowed headers in CORS configuration.
     */
    @Test
    void testCorsConfigurationSource_ShouldAllowAllHeaders() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
        
        var corsConfig = urlBasedSource.getCorsConfigurations().get("/**");
        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedHeaders());
        assertTrue(corsConfig.getAllowedHeaders().contains("*"));
    }

    /**
     * Test that multiple calls to passwordEncoder return different instances.
     * Verifies that bean creation produces new instances.
     */
    @Test
    void testPasswordEncoder_MultipleCallsShouldReturnDifferentInstances() {
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();

        assertNotSame(encoder1, encoder2);
    }

    /**
     * Test that passwordEncoder handles empty passwords.
     * Verifies encoding of edge case inputs.
     */
    @Test
    void testPasswordEncoder_ShouldHandleEmptyPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        String encodedPassword = passwordEncoder.encode(emptyPassword);

        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    /**
     * Test that passwordEncoder produces different hashes for same password.
     * Verifies salt usage in BCrypt.
     */
    @Test
    void testPasswordEncoder_ShouldProduceDifferentHashesForSamePassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "testPassword123";

        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        assertNotEquals(hash1, hash2);
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }

    /**
     * Test that passwordEncoder handles special characters.
     * Verifies encoding of complex passwords.
     */
    @Test
    void testPasswordEncoder_ShouldHandleSpecialCharacters() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String complexPassword = "P@ssw0rd!#$%^&*()_+{}[]|:;<>?,./~`";

        String encodedPassword = passwordEncoder.encode(complexPassword);

        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(complexPassword, encodedPassword));
    }

    /**
     * Test that passwordEncoder handles very long passwords.
     * Verifies encoding of edge case password lengths.
     */
    @Test
    void testPasswordEncoder_ShouldHandleLongPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(100);

        String encodedPassword = passwordEncoder.encode(longPassword);

        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }
}