package com.ecommerce.repository;

import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for RefreshTokenRepository.
 * Tests repository methods for RefreshToken entity operations including token management.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenRepository Tests")
public class test_RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private RefreshToken testToken;
    private String tokenString;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test User and RefreshToken entities.
     */
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        // Create test refresh token
        tokenString = UUID.randomUUID().toString();
        testToken = new RefreshToken();
        testToken.setToken(tokenString);
        testToken.setUserId(testUser.getUserId());
        testToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(testToken);
        
        entityManager.flush();
    }

    /**
     * Test finding a RefreshToken by token string when it exists.
     * Verifies that the correct RefreshToken is returned.
     */
    @Test
    @DisplayName("Should find RefreshToken by token string when exists")
    void testFindByToken_WhenExists_ReturnsToken() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenString);
        assertThat(result.get().getUserId()).isEqualTo(testUser.getUserId());
    }

    /**
     * Test finding a RefreshToken by token string when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token does not exist")
    void testFindByToken_WhenNotExists_ReturnsEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("nonexistent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting RefreshTokens by user ID.
     * Verifies that all tokens for a user are removed.
     */
    @Test
    @DisplayName("Should delete RefreshTokens by user ID")
    void testDeleteByUserId_Success() {
        // When
        refreshTokenRepository.deleteByUserId(testUser.getUserId());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenString);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting RefreshTokens by user ID when no tokens exist.
     * Verifies that no exception is thrown.
     */
    @Test
    @DisplayName("Should handle delete by user ID when no tokens exist")
    void testDeleteByUserId_WhenNoTokens_NoException() {
        // Given
        Long nonExistentUserId = 99999L;

        // When & Then - should not throw exception
        refreshTokenRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();
    }

    /**
     * Test deleting expired RefreshTokens.
     * Verifies that only expired tokens are removed.
     */
    @Test
    @DisplayName("Should delete expired RefreshTokens")
    void testDeleteExpiredTokens_RemovesExpiredOnly() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setUserId(testUser.getUserId());
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> validToken = refreshTokenRepository.findByToken(tokenString);
        Optional<RefreshToken> removedToken = refreshTokenRepository.findByToken(expiredToken.getToken());
        
        assertThat(validToken).isPresent();
        assertThat(removedToken).isEmpty();
    }

    /**
     * Test deleting expired RefreshTokens when none are expired.
     * Verifies that valid tokens are not removed.
     */
    @Test
    @DisplayName("Should not delete valid tokens when deleting expired")
    void testDeleteExpiredTokens_WhenNoneExpired_KeepsAllTokens() {
        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenString);
        assertThat(result).isPresent();
    }

    /**
     * Test saving a new RefreshToken.
     * Verifies that the RefreshToken is persisted correctly.
     */
    @Test
    @DisplayName("Should save new RefreshToken successfully")
    void testSave_NewToken_Success() {
        // Given
        String newTokenString = UUID.randomUUID().toString();
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(newTokenString);
        newToken.setUserId(testUser.getUserId());
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        // When
        RefreshToken savedToken = refreshTokenRepository.save(newToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(newTokenString);
    }

    /**
     * Test updating an existing RefreshToken.
     * Verifies that RefreshToken modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing RefreshToken successfully")
    void testSave_UpdateToken_Success() {
        // Given
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);
        testToken.setExpiryDate(newExpiryDate);

        // When
        RefreshToken updatedToken = refreshTokenRepository.save(testToken);
        entityManager.flush();

        // Then
        assertThat(updatedToken.getExpiryDate()).isEqualToIgnoringNanos(newExpiryDate);
    }

    /**
     * Test deleting a RefreshToken by ID.
     * Verifies that the RefreshToken is removed from the database.
     */
    @Test
    @DisplayName("Should delete RefreshToken by ID successfully")
    void testDeleteById_Success() {
        // Given
        Long tokenId = testToken.getId();

        // When
        refreshTokenRepository.deleteById(tokenId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a RefreshToken by ID.
     * Verifies that the correct RefreshToken is retrieved.
     */
    @Test
    @DisplayName("Should find RefreshToken by ID when exists")
    void testFindById_WhenExists_ReturnsToken() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(testToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testToken.getId());
        assertThat(result.get().getToken()).isEqualTo(tokenString);
    }

    /**
     * Test that multiple tokens can exist for the same user.
     * Verifies user can have multiple refresh tokens.
     */
    @Test
    @DisplayName("Should allow multiple tokens for same user")
    void testMultipleTokensPerUser() {
        // Given
        String anotherTokenString = UUID.randomUUID().toString();
        RefreshToken anotherToken = new RefreshToken();
        anotherToken.setToken(anotherTokenString);
        anotherToken.setUserId(testUser.getUserId());
        anotherToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> token1 = refreshTokenRepository.findByToken(tokenString);
        Optional<RefreshToken> token2 = refreshTokenRepository.findByToken(anotherTokenString);

        // Then
        assertThat(token1).isPresent();
        assertThat(token2).isPresent();
        assertThat(token1.get().getUserId()).isEqualTo(token2.get().getUserId());
    }

    /**
     * Test token expiry date validation.
     * Verifies that tokens with past expiry dates are identified correctly.
     */
    @Test
    @DisplayName("Should correctly identify expired tokens")
    void testTokenExpiryValidation() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setUserId(testUser.getUserId());
        expiredToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(expiredToken.getToken());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getExpiryDate()).isBefore(LocalDateTime.now());
    }

    /**
     * Test deleting all tokens for a user with multiple tokens.
     * Verifies that all user tokens are removed.
     */
    @Test
    @DisplayName("Should delete all tokens for user")
    void testDeleteByUserId_WithMultipleTokens_DeletesAll() {
        // Given
        RefreshToken token2 = new RefreshToken();
        token2.setToken(UUID.randomUUID().toString());
        token2.setUserId(testUser.getUserId());
        token2.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(token2);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteByUserId(testUser.getUserId());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result1 = refreshTokenRepository.findByToken(tokenString);
        Optional<RefreshToken> result2 = refreshTokenRepository.findByToken(token2.getToken());
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
    }
}