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
 * JUnit test class for JwtTokenProvider.
 * Tests JWT token generation, validation, and extraction functionality.
 *
 * @author QA Automation Team
 * @version 1.0
 */
class test_JwtTokenProvider {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret = "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong";
    private Long testExpiration = 86400000L; // 24 hours

    /**
     * Setup method to initialize JwtTokenProvider with test configuration.
     */
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    /**
     * Test generateToken with valid username and userId.
     * Verifies that a valid JWT token is generated with correct claims.
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
     * Test getUserIdFromToken with valid token.
     * Verifies that the correct user ID is extracted from the token.
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
        assertNotNull(extractedUserId, "Extracted user ID should not be null");
        assertEquals(userId, extractedUserId, "Extracted user ID should match the original");
    }

    /**
     * Test getUsernameFromToken with valid token.
     * Verifies that the correct username is extracted from the token.
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
        assertNotNull(extractedUsername, "Extracted username should not be null");
        assertEquals(username, extractedUsername, "Extracted username should match the original");
    }

    /**
     * Test validateToken with valid token.
     * Verifies that a valid token passes validation.
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
        assertTrue(isValid, "Valid token should pass validation");
    }

    /**
     * Test validateToken with invalid token.
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
     * Test validateToken with null token.
     * Verifies that null token is handled gracefully.
     */
    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid, "Null token should fail validation");
    }

    /**
     * Test validateToken with empty token.
     * Verifies that empty token is handled gracefully.
     */
    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid, "Empty token should fail validation");
    }

    /**
     * Test validateToken with malformed token.
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
     * Test validateToken with expired token.
     * Verifies that expired token fails validation.
     */
    @Test
    void testValidateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpiration", -1000L); // Negative expiration
        
        String username = "testuser";
        Long userId = 123L;
        String expiredToken = expiredTokenProvider.generateToken(username, userId);

        // Wait a moment to ensure token is expired
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(isValid, "Expired token should fail validation");
    }

    /**
     * Test getExpirationTime.
     * Verifies that the correct expiration time is returned.
     */
    @Test
    void testGetExpirationTime_ShouldReturnCorrectValue() {
        // Act
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertNotNull(expirationTime, "Expiration time should not be null");
        assertEquals(testExpiration, expirationTime, "Expiration time should match configured value");
    }

    /**
     * Test generateToken with null username.
     * Verifies that token generation handles null username.
     */
    @Test
    void testGenerateToken_WithNullUsername_ShouldGenerateToken() {
        // Arrange
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(null, userId);

        // Assert
        assertNotNull(token, "Token should be generated even with null username");
    }

    /**
     * Test generateToken with null userId.
     * Verifies that token generation handles null userId.
     */
    @Test
    void testGenerateToken_WithNullUserId_ShouldGenerateToken() {
        // Arrange
        String username = "testuser";

        // Act & Assert
        assertDoesNotThrow(() -> jwtTokenProvider.generateToken(username, null),
                "Token generation should handle null userId");
    }

    /**
     * Test getUserIdFromToken with tampered token.
     * Verifies that tampered token throws exception.
     */
    @Test
    void testGetUserIdFromToken_WithTamperedToken_ShouldThrowException() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtTokenProvider.getUserIdFromToken(tamperedToken),
                "Tampered token should throw exception");
    }

    /**
     * Test getUsernameFromToken with tampered token.
     * Verifies that tampered token throws exception.
     */
    @Test
    void testGetUsernameFromToken_WithTamperedToken_ShouldThrowException() {
        // Arrange
        String username = "testuser";
        Long userId = 123L;
        String token = jwtTokenProvider.generateToken(username, userId);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtTokenProvider.getUsernameFromToken(tamperedToken),
                "Tampered token should throw exception");
    }

    /**
     * Test token generation with special characters in username.
     * Verifies that special characters are handled correctly.
     */
    @Test
    void testGenerateToken_WithSpecialCharactersInUsername_ShouldGenerateValidToken() {
        // Arrange
        String username = "test@user.com";
        Long userId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token, "Token should be generated");
        assertEquals(username, extractedUsername, "Username with special characters should be preserved");
    }
}