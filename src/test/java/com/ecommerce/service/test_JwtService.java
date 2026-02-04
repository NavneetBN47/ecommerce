package com.ecommerce.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test class for JwtService.
 * Tests all public methods for JWT token generation, validation, and extraction.
 * Covers normal execution paths, edge cases, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class test_JwtService {

    private JwtService jwtService;
    private final String testSecret = "myTestSecretKeyForJWTTokenGenerationAndValidation12345";
    private final Long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "expiration", testExpiration);
    }

    /**
     * Test successful JWT token generation.
     * Verifies that the service generates a valid JWT token with correct claims.
     */
    @Test
    @DisplayName("Should generate JWT token successfully")
    void testGenerateToken_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";

        // When
        String token = jwtService.generateToken(userId, username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        
        // Verify token can be parsed
        Claims claims = jwtService.extractClaims(token);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(username, claims.get("username", String.class));
    }

    /**
     * Test JWT token generation with null values.
     * Verifies that the service handles null parameters appropriately.
     */
    @Test
    @DisplayName("Should generate token with null userId")
    void testGenerateToken_NullUserId() {
        // Given
        Long userId = null;
        String username = "testuser";

        // When
        String token = jwtService.generateToken(userId, username);

        // Then
        assertNotNull(token);
        Claims claims = jwtService.extractClaims(token);
        assertEquals(username, claims.getSubject());
        assertNull(claims.get("userId", Long.class));
    }

    /**
     * Test JWT token generation with null username.
     * Verifies that the service handles null username appropriately.
     */
    @Test
    @DisplayName("Should generate token with null username")
    void testGenerateToken_NullUsername() {
        // Given
        Long userId = 1L;
        String username = null;

        // When
        String token = jwtService.generateToken(userId, username);

        // Then
        assertNotNull(token);
        Claims claims = jwtService.extractClaims(token);
        assertNull(claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
    }

    /**
     * Test successful claims extraction from valid token.
     * Verifies that the service extracts claims correctly from a valid token.
     */
    @Test
    @DisplayName("Should extract claims from valid token")
    void testExtractClaims_ValidToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        Claims claims = jwtService.extractClaims(token);

        // Then
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(username, claims.get("username", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    /**
     * Test claims extraction from malformed token.
     * Verifies that the service throws appropriate exception for malformed tokens.
     */
    @Test
    @DisplayName("Should throw exception for malformed token")
    void testExtractClaims_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractClaims(malformedToken);
        });
    }

    /**
     * Test claims extraction from token with invalid signature.
     * Verifies that the service throws appropriate exception for invalid signature.
     */
    @Test
    @DisplayName("Should throw exception for invalid signature")
    void testExtractClaims_InvalidSignature() {
        // Given
        // Create token with different secret
        JwtService differentSecretService = new JwtService();
        ReflectionTestUtils.setField(differentSecretService, "secret", "differentSecret123456789");
        ReflectionTestUtils.setField(differentSecretService, "expiration", testExpiration);
        String tokenWithDifferentSignature = differentSecretService.generateToken(1L, "testuser");

        // When & Then
        assertThrows(SignatureException.class, () -> {
            jwtService.extractClaims(tokenWithDifferentSignature);
        });
    }

    /**
     * Test successful user ID extraction from token.
     * Verifies that the service extracts user ID correctly.
     */
    @Test
    @DisplayName("Should extract user ID from token")
    void testExtractUserId_Success() {
        // Given
        Long userId = 123L;
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);
    }

    /**
     * Test user ID extraction when userId claim is null.
     * Verifies that the service handles null userId claim appropriately.
     */
    @Test
    @DisplayName("Should return null when userId claim is null")
    void testExtractUserId_NullClaim() {
        // Given
        Long userId = null;
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertNull(extractedUserId);
    }

    /**
     * Test successful username extraction from token.
     * Verifies that the service extracts username correctly.
     */
    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    /**
     * Test username extraction when subject is null.
     * Verifies that the service handles null subject appropriately.
     */
    @Test
    @DisplayName("Should return null when subject is null")
    void testExtractUsername_NullSubject() {
        // Given
        Long userId = 1L;
        String username = null;
        String token = jwtService.generateToken(userId, username);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertNull(extractedUsername);
    }

    /**
     * Test token validation for valid token.
     * Verifies that the service correctly validates a valid token.
     */
    @Test
    @DisplayName("Should validate valid token")
    void testIsTokenValid_ValidToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    /**
     * Test token validation for malformed token.
     * Verifies that the service correctly identifies malformed tokens as invalid.
     */
    @Test
    @DisplayName("Should invalidate malformed token")
    void testIsTokenValid_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.format";

        // When
        boolean isValid = jwtService.isTokenValid(malformedToken);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test token validation for null token.
     * Verifies that the service correctly handles null token.
     */
    @Test
    @DisplayName("Should invalidate null token")
    void testIsTokenValid_NullToken() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtService.isTokenValid(nullToken);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test token validation for empty token.
     * Verifies that the service correctly handles empty token.
     */
    @Test
    @DisplayName("Should invalidate empty token")
    void testIsTokenValid_EmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtService.isTokenValid(emptyToken);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test token validation for token with invalid signature.
     * Verifies that the service correctly identifies tokens with invalid signatures.
     */
    @Test
    @DisplayName("Should invalidate token with wrong signature")
    void testIsTokenValid_InvalidSignature() {
        // Given
        JwtService differentSecretService = new JwtService();
        ReflectionTestUtils.setField(differentSecretService, "secret", "differentSecret123456789");
        ReflectionTestUtils.setField(differentSecretService, "expiration", testExpiration);
        String tokenWithWrongSignature = differentSecretService.generateToken(1L, "testuser");

        // When
        boolean isValid = jwtService.isTokenValid(tokenWithWrongSignature);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test token expiration validation.
     * Verifies that the service correctly handles expired tokens.
     */
    @Test
    @DisplayName("Should invalidate expired token")
    void testIsTokenValid_ExpiredToken() {
        // Given
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "secret", testSecret);
        ReflectionTestUtils.setField(shortExpirationService, "expiration", 1L); // 1ms expiration
        
        String token = shortExpirationService.generateToken(1L, "testuser");
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test token generation with special characters in username.
     * Verifies that the service handles special characters correctly.
     */
    @Test
    @DisplayName("Should handle special characters in username")
    void testGenerateToken_SpecialCharacters() {
        // Given
        Long userId = 1L;
        String username = "test@user.com";

        // When
        String token = jwtService.generateToken(userId, username);

        // Then
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    /**
     * Test token generation with very long username.
     * Verifies that the service handles long usernames correctly.
     */
    @Test
    @DisplayName("Should handle long username")
    void testGenerateToken_LongUsername() {
        // Given
        Long userId = 1L;
        String longUsername = "a".repeat(1000); // Very long username

        // When
        String token = jwtService.generateToken(userId, longUsername);

        // Then
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(longUsername, extractedUsername);
    }
}