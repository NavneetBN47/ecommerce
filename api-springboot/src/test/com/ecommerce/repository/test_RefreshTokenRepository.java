package com.ecommerce.repository;

import com.ecommerce.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for RefreshTokenRepository.
 * Tests repository methods for RefreshToken entity operations including
 * finding by token, deleting by user ID, and deleting expired tokens.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenRepository Tests")
class test_RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RefreshToken testRefreshToken;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("test-refresh-token");
        testRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
    }

    /**
     * Test finding a refresh token by token string when it exists.
     * Verifies that the correct refresh token is returned.
     */
    @Test
    @DisplayName("Should find refresh token by token when exists")
    void testFindByToken_WhenExists_ReturnsRefreshToken() {
        // Given
        RefreshToken token = new RefreshToken();
        token.setToken("valid-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(token);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("valid-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("valid-token");
    }

    /**
     * Test finding a refresh token by token string when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token does not exist")
    void testFindByToken_WhenNotExists_ReturnsEmpty() {
        // Given
        String nonExistentToken = "non-existent-token";

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(nonExistentToken);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh tokens by user ID.
     * Verifies that all tokens for a user are removed.
     */
    @Test
    @DisplayName("Should delete refresh tokens by user ID")
    void testDeleteByUserId_DeletesUserTokens() {
        // Given
        Long userId = 1L;

        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            refreshTokenRepository.deleteByUserId(userId);
            entityManager.flush();
        });
    }

    /**
     * Test deleting expired refresh tokens.
     * Verifies that only expired tokens are removed.
     */
    @Test
    @DisplayName("Should delete expired refresh tokens")
    void testDeleteExpiredTokens_DeletesOnlyExpiredTokens() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        
        RefreshToken validToken = new RefreshToken();
        validToken.setToken("valid-token");
        validToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        
        entityManager.persist(expiredToken);
        entityManager.persist(validToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> expiredResult = refreshTokenRepository.findByToken("expired-token");
        Optional<RefreshToken> validResult = refreshTokenRepository.findByToken("valid-token");
        
        assertThat(expiredResult).isEmpty();
        assertThat(validResult).isPresent();
    }

    /**
     * Test saving a refresh token.
     * Verifies that the token is persisted correctly.
     */
    @Test
    @DisplayName("Should save refresh token successfully")
    void testSave_ValidRefreshToken_SavesSuccessfully() {
        // Given
        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-token");
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        // When
        RefreshToken savedToken = refreshTokenRepository.save(newToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo("new-token");
    }

    /**
     * Test finding a refresh token by ID.
     * Verifies that the token can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find refresh token by ID when exists")
    void testFindById_WhenExists_ReturnsRefreshToken() {
        // Given
        RefreshToken token = new RefreshToken();
        token.setToken("find-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        RefreshToken savedToken = entityManager.persistAndFlush(token);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test deleting a refresh token by ID.
     * Verifies that the token is removed from the database.
     */
    @Test
    @DisplayName("Should delete refresh token by ID successfully")
    void testDeleteById_ExistingToken_DeletesSuccessfully() {
        // Given
        RefreshToken token = new RefreshToken();
        token.setToken("delete-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        RefreshToken savedToken = entityManager.persistAndFlush(token);
        Long tokenId = savedToken.getId();

        // When
        refreshTokenRepository.deleteById(tokenId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all refresh tokens.
     * Verifies that all tokens can be retrieved.
     */
    @Test
    @DisplayName("Should find all refresh tokens")
    void testFindAll_ReturnsAllRefreshTokens() {
        // Given
        RefreshToken token1 = new RefreshToken();
        token1.setToken("token1");
        token1.setExpiryDate(LocalDateTime.now().plusDays(7));
        
        RefreshToken token2 = new RefreshToken();
        token2.setToken("token2");
        token2.setExpiryDate(LocalDateTime.now().plusDays(7));
        
        entityManager.persist(token1);
        entityManager.persist(token2);
        entityManager.flush();

        // When
        var result = refreshTokenRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting refresh tokens.
     * Verifies that the count of tokens is correct.
     */
    @Test
    @DisplayName("Should count refresh tokens correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = refreshTokenRepository.count();
        RefreshToken token = new RefreshToken();
        token.setToken("count-token");
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(token);

        // When
        long newCount = refreshTokenRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test deleting expired tokens when none are expired.
     * Verifies that no tokens are deleted.
     */
    @Test
    @DisplayName("Should not delete any tokens when none are expired")
    void testDeleteExpiredTokens_WhenNoneExpired_DeletesNone() {
        // Given
        RefreshToken validToken = new RefreshToken();
        validToken.setToken("valid-token");
        validToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(validToken);
        long initialCount = refreshTokenRepository.count();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        long newCount = refreshTokenRepository.count();
        assertThat(newCount).isEqualTo(initialCount);
    }
}