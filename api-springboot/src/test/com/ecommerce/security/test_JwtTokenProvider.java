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
 * Test class for JwtTokenProvider.
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
     * Test successful token generation with valid username and userId.
     * Verifies that token is not null and not empty.
     */
    @Test
    void testGenerateToken_WithValidInputs_ShouldReturnToken() {
        String username = "testuser";
        Long userId = 123L;

        String token = jwtTokenProvider.generateToken(username, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    /**
     * Test token generation with different user IDs.
     * Verifies that different tokens are generated for different users.
     */
    @Test
    void testGenerateToken_WithDifferentUserIds_ShouldGenerateDifferentTokens() {
        String username = "testuser";
        Long userId1 = 123L;
        Long userId2 = 456L;

        String token1 = jwtTokenProvider.generateToken(username, userId1);
        String token2 = jwtTokenProvider.generateToken(username, userId2);

        assertNotEquals(token1, token2);
    }

    /**
     * Test extracting user ID from a valid token.
     * Verifies that the correct user ID is extracted.
     */
    @Test
    void testGetUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    /**
     * Test extracting username from a valid token.
     * Verifies that the correct username is extracted.
     */
    @Test
    void testGetUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    /**
     * Test token validation with a valid token.
     * Verifies that validation returns true.
     */
    @Test
    void testValidateToken_WithValidToken_ShouldReturnTrue() {
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    /**
     * Test token validation with an invalid token.
     * Verifies that validation returns false.
     */
    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    /**
     * Test token validation with a null token.
     * Verifies that validation returns false.
     */
    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    /**
     * Test token validation with an empty token.
     * Verifies that validation returns false.
     */
    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid);
    }

    /**
     * Test token validation with a malformed token.
     * Verifies that validation returns false and doesn't throw exception.
     */
    @Test
    void testValidateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "malformed.token";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertFalse(isValid);
    }

    /**
     * Test token validation with a token signed with different secret.
     * Verifies that validation returns false.
     */
    @Test
    void testValidateToken_WithDifferentSecret_ShouldReturnFalse() {
        String differentSecret = "differentSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong";
        Key differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + testExpiration);
        
        String tokenWithDifferentSecret = Jwts.builder()
            .setSubject("123")
            .claim("username", "testuser")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(differentKey)
            .compact();

        boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSecret);

        assertFalse(isValid);
    }

    /**
     * Test getting expiration time.
     * Verifies that the correct expiration time is returned.
     */
    @Test
    void testGetExpirationTime_ShouldReturnConfiguredExpiration() {
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        assertEquals(testExpiration, expirationTime);
    }

    /**
     * Test token generation with null username.
     * Verifies that token is still generated (username is a claim, not subject).
     */
    @Test
    void testGenerateToken_WithNullUsername_ShouldGenerateToken() {
        Long userId = 123L;

        String token = jwtTokenProvider.generateToken(null, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    /**
     * Test extracting user ID from token with large user ID value.
     * Verifies that large numbers are handled correctly.
     */
    @Test
    void testGetUserIdFromToken_WithLargeUserId_ShouldReturnCorrectValue() {
        String username = "testuser";
        Long largeUserId = Long.MAX_VALUE - 1000;
        String token = jwtTokenProvider.generateToken(username, largeUserId);

        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(largeUserId, extractedUserId);
    }

    /**
     * Test that generated token contains all expected claims.
     * Verifies token structure and claims presence.
     */
    @Test
    void testGenerateToken_ShouldContainAllExpectedClaims() {
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(username, claims.get("username", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    /**
     * Test that token expiration is set correctly.
     * Verifies that expiration date is approximately correct.
     */
    @Test
    void testGenerateToken_ShouldSetCorrectExpiration() {
        String username = "testuser";
        Long userId = 123L;
        Date beforeGeneration = new Date();
        
        String token = jwtTokenProvider.generateToken(username, userId);
        
        Date afterGeneration = new Date();

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();

        Date expiration = claims.getExpiration();
        long expectedExpirationMin = beforeGeneration.getTime() + testExpiration;
        long expectedExpirationMax = afterGeneration.getTime() + testExpiration;

        assertTrue(expiration.getTime() >= expectedExpirationMin);
        assertTrue(expiration.getTime() <= expectedExpirationMax);
    }
}