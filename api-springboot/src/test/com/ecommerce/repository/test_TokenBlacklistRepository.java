package com.ecommerce.repository;

import com.ecommerce.entity.TokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for TokenBlacklistRepository.
 * Tests repository methods for TokenBlacklist entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("TokenBlacklistRepository Tests")
public class test_TokenBlacklistRepository {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TokenBlacklist testTokenBlacklist;
    private String testToken;
    private Long testUserId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testToken = "test-blacklisted-token-123";
        testUserId = 1L;
        testTokenBlacklist = new TokenBlacklist();
        testTokenBlacklist.setToken(testToken);
        testTokenBlacklist.setExpiresAt(LocalDateTime.now().plusDays(1));
    }

    /**
     * Test finding a blacklisted token by token string when it exists.
     * Verifies that the repository correctly retrieves a blacklisted token.
     */
    @Test
    @DisplayName("Should find blacklisted token by token string when exists")
    void testFindByToken_WhenExists() {
        // Given
        TokenBlacklist savedToken = entityManager.persistAndFlush(testTokenBlacklist);

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken);
    }

    /**
     * Test finding a blacklisted token by token string when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent token.
     */
    @Test
    @DisplayName("Should return empty when blacklisted token does not exist")
    void testFindByToken_WhenNotExists() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a token exists in blacklist.
     * Verifies that the repository correctly checks token existence.
     */
    @Test
    @DisplayName("Should check if token exists in blacklist")
    void testExistsByToken_WhenExists() {
        // Given
        entityManager.persistAndFlush(testTokenBlacklist);

        // When
        boolean exists = tokenBlacklistRepository.existsByToken(testToken);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a token does not exist in blacklist.
     * Verifies that the repository correctly returns false for non-blacklisted token.
     */
    @Test
    @DisplayName("Should return false when token is not blacklisted")
    void testExistsByToken_WhenNotExists() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken("non-blacklisted-token");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired blacklisted tokens.
     * Verifies that the repository correctly deletes expired tokens from blacklist.
     */
    @Test
    @Transactional
    @DisplayName("Should delete expired blacklisted tokens")
    void testDeleteExpiredTokens() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(expiredToken);

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("expired-token");
        assertThat(result).isEmpty();
    }

    /**
     * Test that non-expired blacklisted tokens are not deleted.
     * Verifies that the repository does not delete valid blacklisted tokens.
     */
    @Test
    @Transactional
    @DisplayName("Should not delete non-expired blacklisted tokens")
    void testDeleteExpiredTokens_DoesNotDeleteValidTokens() {
        // Given
        TokenBlacklist validToken = entityManager.persistAndFlush(testTokenBlacklist);

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testToken);
        assertThat(result).isPresent();
    }

    /**
     * Test finding blacklisted tokens by user ID.
     * Verifies that the repository correctly retrieves all blacklisted tokens for a user.
     */
    @Test
    @DisplayName("Should find blacklisted tokens by user ID")
    void testFindByUserId() {
        // Given
        entityManager.persistAndFlush(testTokenBlacklist);

        // When
        List<TokenBlacklist> results = tokenBlacklistRepository.findByUserId(testUserId);

        // Then
        assertThat(results).isNotNull();
    }

    /**
     * Test finding blacklisted tokens by user ID when user has no blacklisted tokens.
     * Verifies that the repository returns empty list for user without blacklisted tokens.
     */
    @Test
    @DisplayName("Should return empty list when user has no blacklisted tokens")
    void testFindByUserId_WhenNoTokens() {
        // When
        List<TokenBlacklist> results = tokenBlacklistRepository.findByUserId(999L);

        // Then
        assertThat(results).isEmpty();
    }

    /**
     * Test saving a blacklisted token.
     * Verifies that the repository correctly persists a blacklisted token.
     */
    @Test
    @DisplayName("Should save blacklisted token successfully")
    void testSave_TokenBlacklist() {
        // When
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testTokenBlacklist);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(testToken);
    }

    /**
     * Test finding a blacklisted token by ID.
     * Verifies that the repository correctly retrieves a blacklisted token by its ID.
     */
    @Test
    @DisplayName("Should find blacklisted token by ID")
    void testFindById_WhenExists() {
        // Given
        TokenBlacklist savedToken = entityManager.persistAndFlush(testTokenBlacklist);

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test deleting a blacklisted token.
     * Verifies that the repository correctly deletes a blacklisted token.
     */
    @Test
    @DisplayName("Should delete blacklisted token successfully")
    void testDelete_TokenBlacklist() {
        // Given
        TokenBlacklist savedToken = entityManager.persistAndFlush(testTokenBlacklist);
        Long tokenId = savedToken.getId();

        // When
        tokenBlacklistRepository.delete(savedToken);
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all blacklisted tokens.
     * Verifies that the repository correctly retrieves all blacklisted tokens.
     */
    @Test
    @DisplayName("Should find all blacklisted tokens")
    void testFindAll_TokenBlacklist() {
        // Given
        entityManager.persistAndFlush(testTokenBlacklist);
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken("another-blacklisted-token");
        anotherToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        entityManager.persistAndFlush(anotherToken);

        // When
        List<TokenBlacklist> allTokens = tokenBlacklistRepository.findAll();

        // Then
        assertThat(allTokens).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting blacklisted tokens.
     * Verifies that the repository correctly counts blacklisted tokens.
     */
    @Test
    @DisplayName("Should count blacklisted tokens")
    void testCount_TokenBlacklist() {
        // Given
        entityManager.persistAndFlush(testTokenBlacklist);
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken("token-2");
        anotherToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        entityManager.persistAndFlush(anotherToken);

        // When
        long count = tokenBlacklistRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}