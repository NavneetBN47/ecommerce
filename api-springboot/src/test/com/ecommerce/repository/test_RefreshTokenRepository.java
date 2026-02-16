package com.ecommerce.repository;

import com.ecommerce.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for RefreshTokenRepository.
 * Tests repository methods for RefreshToken entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("RefreshTokenRepository Tests")
public class test_RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RefreshToken testRefreshToken;
    private String testToken;
    private Long testUserId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testToken = "test-refresh-token-123";
        testUserId = 1L;
        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken(testToken);
        testRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
    }

    /**
     * Test finding a refresh token by token string when it exists.
     * Verifies that the repository correctly retrieves a refresh token by its token string.
     */
    @Test
    @DisplayName("Should find refresh token by token string when exists")
    void testFindByToken_WhenExists() {
        // Given
        RefreshToken savedToken = entityManager.persistAndFlush(testRefreshToken);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken);
    }

    /**
     * Test finding a refresh token by token string when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent token.
     */
    @Test
    @DisplayName("Should return empty when refresh token does not exist")
    void testFindByToken_WhenNotExists() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh tokens by user ID.
     * Verifies that the repository correctly deletes all tokens for a user.
     */
    @Test
    @Transactional
    @DisplayName("Should delete refresh tokens by user ID")
    void testDeleteByUserId() {
        // Given
        RefreshToken savedToken = entityManager.persistAndFlush(testRefreshToken);

        // When
        refreshTokenRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting expired refresh tokens.
     * Verifies that the repository correctly deletes tokens that have expired.
     */
    @Test
    @Transactional
    @DisplayName("Should delete expired refresh tokens")
    void testDeleteExpiredTokens() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(expiredToken);

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("expired-token");
        assertThat(result).isEmpty();
    }

    /**
     * Test that non-expired tokens are not deleted.
     * Verifies that the repository does not delete valid tokens when cleaning expired ones.
     */
    @Test
    @Transactional
    @DisplayName("Should not delete non-expired refresh tokens")
    void testDeleteExpiredTokens_DoesNotDeleteValidTokens() {
        // Given
        RefreshToken validToken = entityManager.persistAndFlush(testRefreshToken);

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken);
        assertThat(result).isPresent();
    }

    /**
     * Test saving a refresh token.
     * Verifies that the repository correctly persists a refresh token.
     */
    @Test
    @DisplayName("Should save refresh token successfully")
    void testSave_RefreshToken() {
        // When
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(testToken);
    }

    /**
     * Test finding a refresh token by ID.
     * Verifies that the repository correctly retrieves a refresh token by its ID.
     */
    @Test
    @DisplayName("Should find refresh token by ID")
    void testFindById_WhenExists() {
        // Given
        RefreshToken savedToken = entityManager.persistAndFlush(testRefreshToken);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test deleting a refresh token.
     * Verifies that the repository correctly deletes a refresh token.
     */
    @Test
    @DisplayName("Should delete refresh token successfully")
    void testDelete_RefreshToken() {
        // Given
        RefreshToken savedToken = entityManager.persistAndFlush(testRefreshToken);
        Long tokenId = savedToken.getId();

        // When
        refreshTokenRepository.delete(savedToken);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all refresh tokens.
     * Verifies that the repository correctly retrieves all refresh tokens.
     */
    @Test
    @DisplayName("Should find all refresh tokens")
    void testFindAll_RefreshTokens() {
        // Given
        entityManager.persistAndFlush(testRefreshToken);
        RefreshToken anotherToken = new RefreshToken();
        anotherToken.setToken("another-token");
        anotherToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(anotherToken);

        // When
        var allTokens = refreshTokenRepository.findAll();

        // Then
        assertThat(allTokens).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test updating a refresh token.
     * Verifies that the repository correctly updates refresh token details.
     */
    @Test
    @DisplayName("Should update refresh token successfully")
    void testUpdate_RefreshToken() {
        // Given
        RefreshToken savedToken = entityManager.persistAndFlush(testRefreshToken);
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);
        savedToken.setExpiryDate(newExpiryDate);

        // When
        RefreshToken updatedToken = refreshTokenRepository.save(savedToken);

        // Then
        assertThat(updatedToken.getExpiryDate()).isEqualTo(newExpiryDate);
    }
}