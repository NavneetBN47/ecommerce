package com.ecommerce.repository;

import com.ecommerce.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for RefreshTokenRepository.
 * Tests repository operations for RefreshToken entity including custom query methods.
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
    private String testToken;
    private Long testUserId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testToken = "test-refresh-token-12345";
        testUserId = 1L;
        
        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken(testToken);
        testRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
    }

    /**
     * Test finding a refresh token by token string when it exists.
     * Verifies that the custom query method returns the correct token.
     */
    @Test
    @DisplayName("Should find refresh token by token string when exists")
    void testFindByToken_WhenExists_ShouldReturnToken() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken);
    }

    /**
     * Test finding a refresh token by token string when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when token does not exist")
    void testFindByToken_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a refresh token with null token string.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null token gracefully")
    void testFindByToken_WithNullToken_ShouldReturnEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh tokens by user ID.
     * Verifies that all tokens for a user are deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete refresh tokens by user ID")
    void testDeleteByUserId_ShouldRemoveTokens() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh tokens by non-existent user ID.
     * Verifies that the operation completes without error.
     */
    @Test
    @Transactional
    @DisplayName("Should handle delete by non-existent user ID gracefully")
    void testDeleteByUserId_WithNonExistentUserId_ShouldNotThrowException() {
        // Given
        Long nonExistentUserId = 999999L;

        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            refreshTokenRepository.deleteByUserId(nonExistentUserId);
            entityManager.flush();
        });
    }

    /**
     * Test deleting expired tokens.
     * Verifies that only expired tokens are deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete expired tokens")
    void testDeleteExpiredTokens_ShouldRemoveExpiredTokens() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        refreshTokenRepository.save(expiredToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("expired-token");
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting expired tokens does not affect valid tokens.
     * Verifies that non-expired tokens remain after cleanup.
     */
    @Test
    @Transactional
    @DisplayName("Should not delete valid tokens when cleaning expired tokens")
    void testDeleteExpiredTokens_ShouldKeepValidTokens() {
        // Given
        RefreshToken validToken = new RefreshToken();
        validToken.setToken("valid-token");
        validToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(validToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("valid-token");
        assertThat(result).isPresent();
    }

    /**
     * Test saving a refresh token.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save refresh token successfully")
    void testSave_ShouldPersistToken() {
        // When
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);
        entityManager.flush();

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
    }

    /**
     * Test finding a refresh token by ID.
     * Verifies that findById returns the correct token.
     */
    @Test
    @DisplayName("Should find refresh token by ID when exists")
    void testFindById_WhenExists_ShouldReturnToken() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);
        entityManager.flush();
        Long savedId = savedToken.getId();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test deleting a refresh token.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete refresh token successfully")
    void testDelete_ShouldRemoveToken() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);
        entityManager.flush();
        Long savedId = savedToken.getId();

        // When
        refreshTokenRepository.delete(savedToken);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedId);
        assertThat(result).isEmpty();
    }
}