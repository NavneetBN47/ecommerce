package com.ecommerce.usermanagement.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT Token Service Tests")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private Algorithm algorithm;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        algorithm = Algorithm.RSA256(publicKey, privateKey);

        jwtTokenService = new JwtTokenService(publicKey, privateKey, "test-issuer", 3600L, 86400L);
    }

    @Test
    @DisplayName("Should generate access token successfully")
    void testGenerateAccessToken_Success() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        String token = jwtTokenService.generateAccessToken(userId, email);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer("test-issuer")
                .build()
                .verify(token);

        assertThat(decodedJWT.getSubject()).isEqualTo(userId.toString());
        assertThat(decodedJWT.getClaim("email").asString()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void testGenerateRefreshToken_Success() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenService.generateRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer("test-issuer")
                .build()
                .verify(token);

        assertThat(decodedJWT.getSubject()).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateToken_Valid() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, "test@example.com");

        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateToken_Invalid() {
        String invalidToken = "invalid.token.here";

        boolean result = jwtTokenService.validateToken(invalidToken);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void testValidateToken_Expired() {
        UUID userId = UUID.randomUUID();
        String expiredToken = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId.toString())
                .withIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .withExpiresAt(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .sign(algorithm);

        boolean result = jwtTokenService.validateToken(expiredToken);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void testGetUserIdFromToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, "test@example.com");

        UUID extractedUserId = jwtTokenService.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should extract email from token")
    void testGetEmailFromToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtTokenService.generateAccessToken(userId, email);

        String extractedEmail = jwtTokenService.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Should blacklist token successfully")
    void testBlacklistToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, "test@example.com");

        jwtTokenService.blacklistToken(token);

        boolean isBlacklisted = jwtTokenService.isTokenBlacklisted(token);
        assertThat(isBlacklisted).isTrue();
    }

    @Test
    @DisplayName("Should reject blacklisted token")
    void testValidateToken_Blacklisted() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, "test@example.com");

        jwtTokenService.blacklistToken(token);
        boolean result = jwtTokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should refresh access token")
    void testRefreshAccessToken() {
        UUID userId = UUID.randomUUID();
        String refreshToken = jwtTokenService.generateRefreshToken(userId);

        String newAccessToken = jwtTokenService.refreshAccessToken(refreshToken, "test@example.com");

        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).isNotEmpty();
        assertThat(jwtTokenService.validateToken(newAccessToken)).isTrue();
    }

    @Test
    @DisplayName("Should get token expiration time")
    void testGetTokenExpiration() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, "test@example.com");

        Date expirationDate = jwtTokenService.getTokenExpiration(token);

        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    @DisplayName("Should check if token is about to expire")
    void testIsTokenExpiringSoon() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, "test@example.com");

        boolean isExpiringSoon = jwtTokenService.isTokenExpiringSoon(token, 7200); // 2 hours

        assertThat(isExpiringSoon).isTrue();
    }

    @Test
    @DisplayName("Should handle token with missing claims")
    void testValidateToken_MissingClaims() {
        String tokenWithoutClaims = JWT.create()
                .withIssuer("test-issuer")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
                .sign(algorithm);

        boolean result = jwtTokenService.validateToken(tokenWithoutClaims);

        assertThat(result).isFalse();
    }
}