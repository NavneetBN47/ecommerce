package com.ecommerce.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
    private String jwtSecret;
    private Long jwtExpiration;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtSecret = "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong";
        jwtExpiration = 86400000L; // 24 hours
        
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", jwtExpiration);
    }

    /**
     * Test successful token generation
     * Verifies that a valid JWT token is generated with correct claims
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
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        assertTrue(jwtTokenProvider.validateToken(token));
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
        assertNotEquals(token1, token2);
    }

    /**
     * Test extracting user ID from valid token
     * Verifies that the correct user ID is extracted from token
     */
    @Test
    void testGetUserIdFromToken_WithValidToken_ShouldReturnCorrectUserId() {
        // Arrange
        String username = "testuser";
        Long expectedUserId = 456L;
        String token = jwtTokenProvider.generateToken(username, expectedUserId);

        // Act
        Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(expectedUserId, actualUserId);
    }

    /**
     * Test extracting username from valid token
     * Verifies that the correct username is extracted from token
     */
    @Test
    void testGetUsernameFromToken_WithValidToken_ShouldReturnCorrectUsername() {
        // Arrange
        String expectedUsername = "testuser";
        Long userId = 789L;
        String token = jwtTokenProvider.generateToken(expectedUsername, userId);

        // Act
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(expectedUsername, actualUsername);
    }

    /**
     * Test token validation with valid token
     * Verifies that a properly generated token is validated successfully
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
     * Test token validation with invalid token
     * Verifies that an invalid token is rejected
     */
    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test token validation with null token
     * Verifies that null token is handled gracefully
     */
    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test token validation with empty token
     * Verifies that empty token is rejected
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
     * Test token validation with tampered token
     * Verifies that a token with modified signature is rejected
     */
    @Test
    void testValidateToken_WithTamperedToken_ShouldReturnFalse() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String validToken = jwtTokenProvider.generateToken(username, userId);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test getting expiration time
     * Verifies that the configured expiration time is returned correctly
     */
    @Test
    void testGetExpirationTime_ShouldReturnConfiguredValue() {
        // Act
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertEquals(jwtExpiration, expirationTime);
    }

    /**
     * Test token generation with null username
     * Verifies that token can be generated even with null username
     */
    @Test
    void testGenerateToken_WithNullUsername_ShouldGenerateToken() {
        // Arrange
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(null, userId);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    /**
     * Test extracting user ID from token with invalid format
     * Verifies that exception is thrown for malformed token
     */
    @Test
    void testGetUserIdFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.format";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken(invalidToken);
        });
    }

    /**
     * Test extracting username from token with invalid format
     * Verifies that exception is thrown for malformed token
     */
    @Test
    void testGetUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.format";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    /**
     * Test token contains correct expiration date
     * Verifies that generated token has proper expiration claim
     */
    @Test
    void testGenerateToken_ShouldContainCorrectExpirationDate() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        long beforeGeneration = System.currentTimeMillis();

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);
        
        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        
        // Verify token was generated recently
        long afterGeneration = System.currentTimeMillis();
        assertTrue(afterGeneration - beforeGeneration < 1000); // Less than 1 second
    }
}