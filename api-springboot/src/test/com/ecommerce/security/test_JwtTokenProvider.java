package com.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for JwtTokenProvider.
 * Tests JWT token generation, validation, and claims extraction functionality.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class test_JwtTokenProvider {

    private JwtTokenProvider jwtTokenProvider;

    private static final String JWT_SECRET = "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong";
    private static final Long JWT_EXPIRATION = 86400000L; // 24 hours
    private static final String TEST_USERNAME = "testuser";
    private static final Long TEST_USER_ID = 123L;

    /**
     * Setup method executed before each test.
     * Initializes JwtTokenProvider with test configuration.
     */
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", JWT_EXPIRATION);
    }

    /**
     * Test successful JWT token generation.
     * Verifies that a valid JWT token is generated with correct claims.
     */
    @Test
    void testGenerateToken_WithValidInputs_ShouldReturnValidToken() {
        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Assert
        assertNotNull(token, "Generated token should not be null");
        assertFalse(token.isEmpty(), "Generated token should not be empty");
        assertTrue(token.split("\\.").length == 3, "JWT token should have three parts");
    }

    /**
     * Test token generation with different usernames.
     * Verifies that tokens are generated for various username formats.
     */
    @Test
    void testGenerateToken_WithDifferentUsernames_ShouldGenerateUniqueTokens() {
        // Arrange
        String username1 = "user1";
        String username2 = "user2";

        // Act
        String token1 = jwtTokenProvider.generateToken(username1, 1L);
        String token2 = jwtTokenProvider.generateToken(username2, 2L);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2, "Tokens for different users should be different");
    }

    /**
     * Test extracting user ID from valid token.
     * Verifies that user ID can be correctly extracted from JWT token.
     */
    @Test
    void testGetUserIdFromToken_WithValidToken_ShouldReturnCorrectUserId() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertNotNull(extractedUserId, "Extracted user ID should not be null");
        assertEquals(TEST_USER_ID, extractedUserId, "Extracted user ID should match the original");
    }

    /**
     * Test extracting username from valid token.
     * Verifies that username can be correctly extracted from JWT token.
     */
    @Test
    void testGetUsernameFromToken_WithValidToken_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(extractedUsername, "Extracted username should not be null");
        assertEquals(TEST_USERNAME, extractedUsername, "Extracted username should match the original");
    }

    /**
     * Test validating a valid token.
     * Verifies that a freshly generated token is validated successfully.
     */
    @Test
    void testValidateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid, "Valid token should pass validation");
    }

    /**
     * Test validating an invalid token.
     * Verifies that an invalid token fails validation.
     */
    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid, "Invalid token should fail validation");
    }

    /**
     * Test validating a null token.
     * Verifies that null token fails validation gracefully.
     */
    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid, "Null token should fail validation");
    }

    /**
     * Test validating an empty token.
     * Verifies that empty token fails validation.
     */
    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid, "Empty token should fail validation");
    }

    /**
     * Test validating a malformed token.
     * Verifies that malformed token fails validation.
     */
    @Test
    void testValidateToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "malformed.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid, "Malformed token should fail validation");
    }

    /**
     * Test validating a token with wrong signature.
     * Verifies that token signed with different key fails validation.
     */
    @Test
    void testValidateToken_WithWrongSignature_ShouldReturnFalse() {
        // Arrange
        Key wrongKey = Keys.hmacShaKeyFor("differentSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong".getBytes());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        
        String tokenWithWrongSignature = Jwts.builder()
            .setSubject(TEST_USER_ID.toString())
            .claim("username", TEST_USERNAME)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(wrongKey)
            .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithWrongSignature);

        // Assert
        assertFalse(isValid, "Token with wrong signature should fail validation");
    }

    /**
     * Test getting expiration time.
     * Verifies that the configured expiration time is returned correctly.
     */
    @Test
    void testGetExpirationTime_ShouldReturnConfiguredValue() {
        // Act
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertNotNull(expirationTime, "Expiration time should not be null");
        assertEquals(JWT_EXPIRATION, expirationTime, "Expiration time should match configured value");
    }

    /**
     * Test token contains correct issued at time.
     * Verifies that token's issued at time is recent.
     */
    @Test
    void testGenerateToken_ShouldContainCorrectIssuedAtTime() {
        // Arrange
        long beforeGeneration = System.currentTimeMillis();
        
        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID);
        long afterGeneration = System.currentTimeMillis();
        
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        long issuedAt = claims.getIssuedAt().getTime();

        // Assert
        assertTrue(issuedAt >= beforeGeneration && issuedAt <= afterGeneration,
            "Issued at time should be within generation time range");
    }

    /**
     * Test token contains correct expiration time.
     * Verifies that token's expiration is set correctly based on configuration.
     */
    @Test
    void testGenerateToken_ShouldContainCorrectExpirationTime() {
        // Arrange
        long beforeGeneration = System.currentTimeMillis();
        
        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID);
        
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        long expiration = claims.getExpiration().getTime();
        long expectedExpiration = beforeGeneration + JWT_EXPIRATION;

        // Assert
        assertTrue(Math.abs(expiration - expectedExpiration) < 1000,
            "Expiration time should be approximately issued time + expiration duration");
    }

    /**
     * Test extracting user ID from token with invalid format.
     * Verifies that exception is thrown for invalid token format.
     */
    @Test
    void testGetUserIdFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.format";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken(invalidToken);
        }, "Should throw exception for invalid token");
    }

    /**
     * Test extracting username from token with invalid format.
     * Verifies that exception is thrown for invalid token format.
     */
    @Test
    void testGetUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.format";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        }, "Should throw exception for invalid token");
    }

    /**
     * Test token generation with special characters in username.
     * Verifies that special characters are handled correctly.
     */
    @Test
    void testGenerateToken_WithSpecialCharactersInUsername_ShouldGenerateValidToken() {
        // Arrange
        String specialUsername = "user@example.com";

        // Act
        String token = jwtTokenProvider.generateToken(specialUsername, TEST_USER_ID);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(specialUsername, extractedUsername);
    }

    /**
     * Test token generation with large user ID.
     * Verifies that large user IDs are handled correctly.
     */
    @Test
    void testGenerateToken_WithLargeUserId_ShouldGenerateValidToken() {
        // Arrange
        Long largeUserId = Long.MAX_VALUE;

        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME, largeUserId);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(largeUserId, extractedUserId);
    }
}