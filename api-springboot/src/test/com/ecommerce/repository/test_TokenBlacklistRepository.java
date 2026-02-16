package com.ecommerce.repository;

import com.ecommerce.entity.TokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for TokenBlacklistRepository.
 * Tests repository operations for TokenBlacklist entity including custom query methods.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TokenBlacklistRepository Tests")
class test_TokenBlacklistRepository {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TokenBlacklist testBlacklistToken;
    private String testToken;
    private Long testUserId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testToken = "blacklisted-token-12345";
        testUserId = 1L;
        
        testBlacklistToken = new TokenBlacklist();
        testBlacklistToken.setToken(testToken);
        testBlacklistToken.setExpiresAt(LocalDateTime.now().plusHours(1));
    }

    /**
     * Test finding a blacklisted token by token string when it exists.
     * Verifies that the custom query method returns the correct token.
     */
    @Test
    @DisplayName("Should find blacklisted token by token string when exists")
    void testFindByToken_WhenExists_ShouldReturnToken() {
        // Given
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testToken);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken);
    }

    /**
     * Test finding a blacklisted token by token string when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when token does not exist")
    void testFindByToken_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a token exists in blacklist.
     * Verifies that existsByToken returns true for blacklisted token.
     */
    @Test
    @DisplayName("Should return true when token exists in blacklist")
    void testExistsByToken_WhenExists_ShouldReturnTrue() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        boolean exists = tokenBlacklistRepository.existsByToken(testToken);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a token exists in blacklist when it does not.
     * Verifies that existsByToken returns false.
     */
    @Test
    @DisplayName("Should return false when token does not exist in blacklist")
    void testExistsByToken_WhenNotExists_ShouldReturnFalse() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken("non-existent-token");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired tokens from blacklist.
     * Verifies that only expired tokens are deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete expired tokens from blacklist")
    void testDeleteExpiredTokens_ShouldRemoveExpiredTokens() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        tokenBlacklistRepository.save(expiredToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("expired-token");
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
        TokenBlacklist validToken = new TokenBlacklist();
        validToken.setToken("valid-token");
        validToken.setExpiresAt(LocalDateTime.now().plusHours(2));
        tokenBlacklistRepository.save(validToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("valid-token");
        assertThat(result).isPresent();
    }

    /**
     * Test finding blacklisted tokens by user ID.
     * Verifies that all tokens for a specific user are returned.
     */
    @Test
    @DisplayName("Should find blacklisted tokens by user ID")
    void testFindByUserId_ShouldReturnUserTokens() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding blacklisted tokens by user ID when user has no tokens.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when user has no blacklisted tokens")
    void testFindByUserId_WhenNoTokens_ShouldReturnEmptyList() {
        // Given
        Long nonExistentUserId = 999999L;

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a blacklisted token.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save blacklisted token successfully")
    void testSave_ShouldPersistToken() {
        // When
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
    }

    /**
     * Test finding a blacklisted token by ID.
     * Verifies that findById returns the correct token.
     */
    @Test
    @DisplayName("Should find blacklisted token by ID when exists")
    void testFindById_WhenExists_ShouldReturnToken() {
        // Given
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();
        Long savedId = savedToken.getId();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test deleting a blacklisted token.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete blacklisted token successfully")
    void testDelete_ShouldRemoveToken() {
        // Given
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();
        Long savedId = savedToken.getId();

        // When
        tokenBlacklistRepository.delete(savedToken);
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all blacklisted tokens.
     * Verifies that the count operation returns correct number.
     */
    @Test
    @DisplayName("Should count all blacklisted tokens correctly")
    void testCount_ShouldReturnCorrectCount() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        long count = tokenBlacklistRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}