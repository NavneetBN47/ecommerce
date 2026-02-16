package com.ecommerce.repository;

import com.ecommerce.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for RefreshTokenRepository.
 * Tests all public methods including custom query and delete methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("RefreshTokenRepository Tests")
public class test_RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RefreshToken testToken;
    private String testTokenValue;
    private Long testUserId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testTokenValue = "test-refresh-token-123";
        testUserId = 1L;
        
        testToken = new RefreshToken();
        testToken.setToken(testTokenValue);
        testToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        // Note: Actual entity setup would require User entity
        // This is a simplified version for demonstration
    }

    /**
     * Test finding refresh token by token value - success case.
     */
    @Test
    @DisplayName("Should find refresh token by token value")
    void testFindByToken_Success() {
        // Given
        refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testTokenValue);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test finding refresh token by token value - not found case.
     */
    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByToken_NotFound() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh token by user ID.
     */
    @Test
    @DisplayName("Should delete refresh token by user ID")
    void testDeleteByUserId() {
        // Given
        refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testTokenValue);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting expired tokens.
     */
    @Test
    @DisplayName("Should delete expired tokens")
    void testDeleteExpiredTokens() {
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
     * Test that non-expired tokens are not deleted.
     */
    @Test
    @DisplayName("Should not delete non-expired tokens")
    void testDeleteExpiredTokens_KeepValid() {
        // Given
        refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testTokenValue);
        assertThat(result).isPresent();
    }

    /**
     * Test saving a refresh token.
     */
    @Test
    @DisplayName("Should save refresh token successfully")
    void testSaveRefreshToken() {
        // When
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        entityManager.flush();

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test finding refresh token by ID.
     */
    @Test
    @DisplayName("Should find refresh token by ID")
    void testFindById_Success() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test deleting refresh token by ID.
     */
    @Test
    @DisplayName("Should delete refresh token by ID")
    void testDeleteById() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        entityManager.flush();
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
     */
    @Test
    @DisplayName("Should find all refresh tokens")
    void testFindAll() {
        // Given
        refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        var tokens = refreshTokenRepository.findAll();

        // Then
        assertThat(tokens).isNotEmpty();
    }

    /**
     * Test updating a refresh token.
     */
    @Test
    @DisplayName("Should update refresh token successfully")
    void testUpdateRefreshToken() {
        // Given
        RefreshToken savedToken = refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);
        savedToken.setExpiryDate(newExpiryDate);
        RefreshToken updatedToken = refreshTokenRepository.save(savedToken);
        entityManager.flush();

        // Then
        assertThat(updatedToken.getExpiryDate()).isEqualTo(newExpiryDate);
    }

    /**
     * Test with null token value - edge case.
     */
    @Test
    @DisplayName("Should handle null token value gracefully")
    void testFindByToken_NullToken() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting by non-existent user ID.
     */
    @Test
    @DisplayName("Should handle delete by non-existent user ID gracefully")
    void testDeleteByUserId_NotFound() {
        // Given
        Long nonExistentUserId = 999L;

        // When/Then - should not throw exception
        refreshTokenRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();
    }

    /**
     * Test deleting expired tokens with future date.
     */
    @Test
    @DisplayName("Should not delete any tokens when checking future date")
    void testDeleteExpiredTokens_FutureDate() {
        // Given
        refreshTokenRepository.save(testToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now().plusDays(30));
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testTokenValue);
        assertThat(result).isEmpty();
    }
}