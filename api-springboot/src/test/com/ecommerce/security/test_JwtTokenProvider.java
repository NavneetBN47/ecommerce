package com.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * Tests JWT token generation, validation, and claims extraction.
 */
class test_JwtTokenProvider {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret = "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong";
    private Long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    /**
     * Test successful JWT token generation.
     * Verifies that token is generated with correct claims.
     */
    @Test
    void testGenerateToken_WithValidInputs_ShouldReturnValidToken() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    /**
     * Test extracting user ID from valid token.
     * Verifies that correct user ID is extracted from token.
     */
    @Test
    void testGetUserIdFromToken_WithValidToken_ShouldReturnCorrectUserId() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);
    }

    /**
     * Test extracting username from valid token.
     * Verifies that correct username is extracted from token.
     */
    @Test
    void testGetUsernameFromToken_WithValidToken_ShouldReturnCorrectUsername() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    /**
     * Test validating a valid token.
     * Verifies that valid token passes validation.
     */
    @Test
    void testValidateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    /**
     * Test validating an invalid token.
     * Verifies that invalid token fails validation.
     */
    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test validating a malformed token.
     * Verifies that malformed token fails validation.
     */
    @Test
    void testValidateToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "malformed-token-without-dots";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test validating an empty token.
     * Verifies that empty token fails validation.
     */
    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        // Arrange
        String emptyToken = "";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test validating a null token.
     * Verifies that null token fails validation.
     */
    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        // Arrange
        String nullToken = null;

        // Act
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test getting expiration time.
     * Verifies that correct expiration time is returned.
     */
    @Test
    void testGetExpirationTime_ShouldReturnConfiguredExpiration() {
        // Act
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertNotNull(expirationTime);
        assertEquals(testExpiration, expirationTime);
    }

    /**
     * Test token generation with special characters in username.
     * Verifies that tokens can be generated with special characters.
     */
    @Test
    void testGenerateToken_WithSpecialCharactersInUsername_ShouldGenerateValidToken() {
        // Arrange
        String username = "test.user@example.com";
        Long userId = 456L;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
    }

    /**
     * Test token generation with large user ID.
     * Verifies that tokens can handle large user IDs.
     */
    @Test
    void testGenerateToken_WithLargeUserId_ShouldGenerateValidToken() {
        // Arrange
        String username = "testuser";
        Long userId = Long.MAX_VALUE;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
    }

    /**
     * Test extracting user ID from token with wrong secret.
     * Verifies that token signed with different secret fails validation.
     */
    @Test
    void testValidateToken_WithDifferentSecret_ShouldReturnFalse() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        
        // Generate token with original provider
        String token = jwtTokenProvider.generateToken(username, userId);
        
        // Create new provider with different secret
        JwtTokenProvider differentProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(differentProvider, "jwtSecret", "differentSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong");
        ReflectionTestUtils.setField(differentProvider, "jwtExpiration", testExpiration);

        // Act
        boolean isValid = differentProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test token contains correct issued at date.
     * Verifies that token has valid issued at timestamp.
     */
    @Test
    void testGenerateToken_ShouldContainIssuedAtDate() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        Date beforeGeneration = new Date();

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);
        Date afterGeneration = new Date();

        // Assert
        assertNotNull(token);
        // Token should be generated between before and after timestamps
        Key signingKey = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Date issuedAt = claims.getIssuedAt();
        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() >= beforeGeneration.getTime() - 1000); // Allow 1 second tolerance
        assertTrue(issuedAt.getTime() <= afterGeneration.getTime() + 1000);
    }

    /**
     * Test token contains correct expiration date.
     * Verifies that token expiration is set correctly.
     */
    @Test
    void testGenerateToken_ShouldContainCorrectExpirationDate() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        Date beforeGeneration = new Date();

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        Key signingKey = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Date expiration = claims.getExpiration();
        Date expectedExpiration = new Date(beforeGeneration.getTime() + testExpiration);
        
        assertNotNull(expiration);
        // Allow 2 second tolerance for test execution time
        assertTrue(Math.abs(expiration.getTime() - expectedExpiration.getTime()) < 2000);
    }
}