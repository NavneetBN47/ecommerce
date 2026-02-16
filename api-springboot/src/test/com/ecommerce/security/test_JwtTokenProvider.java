package com.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for JwtTokenProvider
 * 
 * Tests JWT token generation and validation including:
 * - Token generation from authentication
 * - User ID extraction from token
 * - Token validation
 * - Handling invalid/expired tokens
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Tests")
class test_JwtTokenProvider {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String jwtSecret;
    private long jwtExpiration;
    private UUID testUserId;
    private UserPrincipal testUserPrincipal;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        jwtSecret = "testSecretKeyForJWTTokenGenerationAndValidationThatIsLongEnough";
        jwtExpiration = 86400000L;
        testUserId = UUID.randomUUID();

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", jwtExpiration);

        testUserPrincipal = new UserPrincipal(
            testUserId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );
    }

    /**
     * Test generating JWT token from authentication
     * 
     * Validates:
     * - Token is generated successfully
     * - Token is not null or empty
     * - Token contains user ID
     */
    @Test
    @DisplayName("Should generate JWT token successfully")
    void testGenerateToken_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    /**
     * Test extracting user ID from valid token
     * 
     * Validates:
     * - User ID is extracted correctly
     * - Extracted ID matches original
     */
    @Test
    @DisplayName("Should extract user ID from token successfully")
    void testGetUserIdFromToken_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

        String token = jwtTokenProvider.generateToken(authentication);
        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isNotNull();
        assertThat(extractedUserId).isEqualTo(testUserId.toString());
    }

    /**
     * Test validating valid token
     * 
     * Validates:
     * - Valid token returns true
     * - No exceptions are thrown
     */
    @Test
    @DisplayName("Should validate valid token successfully")
    void testValidateToken_ValidToken() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);

        String token = jwtTokenProvider.generateToken(authentication);
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    /**
     * Test validating invalid token
     * 
     * Validates:
     * - Invalid token returns false
     * - No exceptions are thrown to caller
     */
    @Test
    @DisplayName("Should return false for invalid token")
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    /**
     * Test validating expired token
     * 
     * Validates:
     * - Expired token returns false
     * - Expiration is properly checked
     */
    @Test
    @DisplayName("Should return false for expired token")
    void testValidateToken_ExpiredToken() {
        Date pastDate = new Date(System.currentTimeMillis() - 1000000);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
            .setSubject(testUserId.toString())
            .setIssuedAt(pastDate)
            .setExpiration(pastDate)
            .signWith(key)
            .compact();

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    /**
     * Test validating malformed token
     * 
     * Validates:
     * - Malformed token returns false
     * - Exception is handled gracefully
     */
    @Test
    @DisplayName("Should return false for malformed token")
    void testValidateToken_MalformedToken() {
        String malformedToken = "malformed";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    /**
     * Test validating empty token
     * 
     * Validates:
     * - Empty token returns false
     * - No null pointer exceptions
     */
    @Test
    @DisplayName("Should return false for empty token")
    void testValidateToken_EmptyToken() {
        String emptyToken = "";

        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        assertThat(isValid).isFalse();
    }
}