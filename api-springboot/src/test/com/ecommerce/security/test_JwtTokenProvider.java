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
 * Test class for JwtTokenProvider
 * Tests JWT token generation, validation, and extraction functionality
 */
class test_JwtTokenProvider {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret = "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForTesting";
    private Long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    /**
     * Test successful JWT token generation
     * Verifies that a valid token is generated with correct claims
     */
    @Test
    void testGenerateToken_WithValidInputs_ShouldReturnValidToken() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        assertNotNull(token, "Generated token should not be null");
        assertFalse(token.isEmpty(), "Generated token should not be empty");
        assertTrue(token.split("\\.").length == 3, "JWT token should have 3 parts");
    }

    /**
     * Test token generation and validation
     * Verifies that generated tokens are valid
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
        assertTrue(isValid, "Generated token should be valid");
    }

    /**
     * Test token validation with invalid token
     * Verifies that invalid tokens are rejected
     */
    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid, "Invalid token should not be validated");
    }

    /**
     * Test token validation with null token
     * Verifies that null tokens are handled gracefully
     */
    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid, "Null token should not be validated");
    }

    /**
     * Test token validation with empty token
     * Verifies that empty tokens are rejected
     */
    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid, "Empty token should not be validated");
    }

    /**
     * Test extracting user ID from valid token
     * Verifies that user ID can be correctly extracted from token
     */
    @Test
    void testGetUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertNotNull(extractedUserId, "Extracted user ID should not be null");
        assertEquals(userId, extractedUserId, "Extracted user ID should match original");
    }

    /**
     * Test extracting username from valid token
     * Verifies that username can be correctly extracted from token
     */
    @Test
    void testGetUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(extractedUsername, "Extracted username should not be null");
        assertEquals(username, extractedUsername, "Extracted username should match original");
    }

    /**
     * Test extracting user ID from invalid token
     * Verifies that exception is thrown for invalid tokens
     */
    @Test
    void testGetUserIdFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken(invalidToken);
        }, "Should throw exception for invalid token");
    }

    /**
     * Test extracting username from invalid token
     * Verifies that exception is thrown for invalid tokens
     */
    @Test
    void testGetUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        }, "Should throw exception for invalid token");
    }

    /**
     * Test getting expiration time
     * Verifies that configured expiration time is returned
     */
    @Test
    void testGetExpirationTime_ShouldReturnConfiguredExpiration() {
        // Act
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertNotNull(expirationTime, "Expiration time should not be null");
        assertEquals(testExpiration, expirationTime, "Expiration time should match configured value");
    }

    /**
     * Test token generation with different user IDs
     * Verifies that tokens are unique for different users
     */
    @Test
    void testGenerateToken_WithDifferentUserIds_ShouldGenerateDifferentTokens() {
        // Arrange
        String username1 = "user1";
        Long userId1 = 1L;
        String username2 = "user2";
        Long userId2 = 2L;

        // Act
        String token1 = jwtTokenProvider.generateToken(username1, userId1);
        String token2 = jwtTokenProvider.generateToken(username2, userId2);

        // Assert
        assertNotEquals(token1, token2, "Tokens for different users should be different");
    }

    /**
     * Test token contains correct expiration date
     * Verifies that token expiration is set correctly
     */
    @Test
    void testGenerateToken_ShouldContainCorrectExpiration() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        Date beforeGeneration = new Date();

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);
        Date afterGeneration = new Date();

        // Assert
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        Date expiration = claims.getExpiration();
        assertNotNull(expiration, "Token should have expiration date");
        
        long expectedExpirationMin = beforeGeneration.getTime() + testExpiration;
        long expectedExpirationMax = afterGeneration.getTime() + testExpiration;
        
        assertTrue(expiration.getTime() >= expectedExpirationMin && 
                   expiration.getTime() <= expectedExpirationMax,
                   "Token expiration should be within expected range");
    }

    /**
     * Test token validation with expired token
     * Verifies that expired tokens are rejected
     */
    @Test
    void testValidateToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Arrange
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpiration", 1L); // 1 millisecond
        
        String username = "testuser";
        Long userId = 123L;
        String token = shortExpirationProvider.generateToken(username, userId);
        
        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = shortExpirationProvider.validateToken(token);

        // Assert
        assertFalse(isValid, "Expired token should not be valid");
    }

    /**
     * Test token generation with null username
     * Verifies that token can be generated even with null username (username is optional claim)
     */
    @Test
    void testGenerateToken_WithNullUsername_ShouldGenerateToken() {
        // Arrange
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(null, userId);

        // Assert
        assertNotNull(token, "Token should be generated even with null username");
        assertTrue(jwtTokenProvider.validateToken(token), "Generated token should be valid");
    }

    /**
     * Test token generation with empty username
     * Verifies that token can be generated with empty username
     */
    @Test
    void testGenerateToken_WithEmptyUsername_ShouldGenerateToken() {
        // Arrange
        String username = "";
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        assertNotNull(token, "Token should be generated with empty username");
        assertTrue(jwtTokenProvider.validateToken(token), "Generated token should be valid");
    }
}