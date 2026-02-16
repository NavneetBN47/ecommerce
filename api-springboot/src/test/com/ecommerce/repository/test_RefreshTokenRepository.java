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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenRepository Tests")
class test_RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RefreshToken testToken;
    private String testTokenValue;
    private Long testUserId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testTokenValue = "test-refresh-token-12345";
        testUserId = 1L;
        
        testToken = new RefreshToken();
        testToken.setToken(testTokenValue);
        testToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        testToken.setUserId(testUserId);
    }

    /**
     * Test finding refresh token by token value when token exists.
     * Verifies that the correct token is returned.
     */
    @Test
    @DisplayName("Should find refresh token by token value when exists")
    void testFindByToken_WhenExists() {
        // Given
        entityManager.persist(testToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testTokenValue);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testTokenValue);
        assertThat(result.get().getUserId()).isEqualTo(testUserId);
    }

    /**
     * Test finding refresh token by token value when token does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token not found")
    void testFindByToken_WhenNotExists() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh token by user ID.
     * Verifies that all tokens for the specified user are removed.
     */
    @Test
    @DisplayName("Should delete refresh token by user ID")
    void testDeleteByUserId() {
        // Given
        entityManager.persist(testToken);
        entityManager.flush();
        entityManager.clear();

        // When
        refreshTokenRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testTokenValue);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh token by user ID when no tokens exist.
     * Verifies that no exception is thrown.
     */
    @Test
    @DisplayName("Should handle delete by user ID when no tokens exist")
    void testDeleteByUserId_WhenNoTokens() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then - should not throw exception
        refreshTokenRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();
    }

    /**
     * Test deleting expired tokens.
     * Verifies that only expired tokens are removed.
     */
    @Test
    @DisplayName("Should delete expired tokens")
    void testDeleteExpiredTokens() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        expiredToken.setUserId(testUserId);
        entityManager.persist(expiredToken);
        
        RefreshToken validToken = new RefreshToken();
        validToken.setToken("valid-token");
        validToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        validToken.setUserId(testUserId);
        entityManager.persist(validToken);
        entityManager.flush();
        entityManager.clear();

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
     * Test deleting expired tokens when no expired tokens exist.
     * Verifies that valid tokens are not affected.
     */
    @Test
    @DisplayName("Should not delete valid tokens when deleting expired tokens")
    void testDeleteExpiredTokens_NoExpiredTokens() {
        // Given
        entityManager.persist(testToken);
        entityManager.flush();
        long countBefore = refreshTokenRepository.count();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        long countAfter = refreshTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test saving a new refresh token.
     * Verifies that the token is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new refresh token successfully")
    void testSave_NewToken() {
        // When
        RefreshToken savedToken = refreshTokenRepository.save(testToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test updating an existing refresh token.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing refresh token successfully")
    void testSave_UpdateToken() {
        // Given
        RefreshToken savedToken = entityManager.persist(testToken);
        entityManager.flush();
        Long savedId = savedToken.getId();
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);

        // When
        savedToken.setExpiryDate(newExpiryDate);
        RefreshToken updatedToken = refreshTokenRepository.save(savedToken);

        // Then
        assertThat(updatedToken.getId()).isEqualTo(savedId);
        assertThat(updatedToken.getExpiryDate()).isEqualToIgnoringNanos(newExpiryDate);
    }

    /**
     * Test finding refresh token by ID.
     * Verifies that the correct token is retrieved.
     */
    @Test
    @DisplayName("Should find refresh token by ID when exists")
    void testFindById_WhenExists() {
        // Given
        RefreshToken savedToken = entityManager.persist(testToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test finding refresh token by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a refresh token by ID.
     * Verifies that the token is removed from the database.
     */
    @Test
    @DisplayName("Should delete refresh token by ID successfully")
    void testDeleteById() {
        // Given
        RefreshToken savedToken = entityManager.persist(testToken);
        entityManager.flush();
        Long savedId = savedToken.getId();

        // When
        refreshTokenRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all refresh tokens.
     * Verifies that all persisted tokens are retrieved.
     */
    @Test
    @DisplayName("Should find all refresh tokens")
    void testFindAll() {
        // Given
        entityManager.persist(testToken);
        
        RefreshToken anotherToken = new RefreshToken();
        anotherToken.setToken("another-token");
        anotherToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        anotherToken.setUserId(2L);
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        var allTokens = refreshTokenRepository.findAll();

        // Then
        assertThat(allTokens).hasSize(2);
    }

    /**
     * Test checking if refresh token exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when token exists by ID")
    void testExistsById_WhenExists() {
        // Given
        RefreshToken savedToken = entityManager.persist(testToken);
        entityManager.flush();

        // When
        boolean exists = refreshTokenRepository.existsById(savedToken.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if refresh token exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when token does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = refreshTokenRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all refresh tokens.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all refresh tokens correctly")
    void testCount() {
        // Given
        entityManager.persist(testToken);
        RefreshToken anotherToken = new RefreshToken();
        anotherToken.setToken("another-token");
        anotherToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        anotherToken.setUserId(2L);
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        long count = refreshTokenRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}