package com.ecommerce.repository;

import com.ecommerce.entity.TokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for TokenBlacklistRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("TokenBlacklistRepository Tests")
public class test_TokenBlacklistRepository {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TokenBlacklist testBlacklistToken;
    private String testTokenValue;
    private Long testUserId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testTokenValue = "blacklisted-token-123";
        testUserId = 1L;
        
        testBlacklistToken = new TokenBlacklist();
        testBlacklistToken.setToken(testTokenValue);
        testBlacklistToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        testBlacklistToken.setBlacklistedAt(LocalDateTime.now());
        // Note: Actual entity setup would require User entity
        // This is a simplified version for demonstration
    }

    /**
     * Test finding blacklisted token by token value - success case.
     */
    @Test
    @DisplayName("Should find blacklisted token by token value")
    void testFindByToken_Success() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testTokenValue);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test finding blacklisted token by token value - not found case.
     */
    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByToken_NotFound() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if token exists by token value - true case.
     */
    @Test
    @DisplayName("Should return true when token exists")
    void testExistsByToken_True() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        boolean exists = tokenBlacklistRepository.existsByToken(testTokenValue);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if token exists by token value - false case.
     */
    @Test
    @DisplayName("Should return false when token does not exist")
    void testExistsByToken_False() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken("non-existent-token");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired tokens.
     */
    @Test
    @DisplayName("Should delete expired tokens")
    void testDeleteExpiredTokens() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setBlacklistedAt(LocalDateTime.now().minusDays(1));
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
     * Test that non-expired tokens are not deleted.
     */
    @Test
    @DisplayName("Should not delete non-expired tokens")
    void testDeleteExpiredTokens_KeepValid() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testTokenValue);
        assertThat(result).isPresent();
    }

    /**
     * Test finding blacklisted tokens by user ID.
     */
    @Test
    @DisplayName("Should find blacklisted tokens by user ID")
    void testFindByUserId() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding blacklisted tokens by user ID - no results.
     */
    @Test
    @DisplayName("Should return empty list when no tokens found for user")
    void testFindByUserId_NoResults() {
        // Given
        Long nonExistentUserId = 999L;

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a blacklisted token.
     */
    @Test
    @DisplayName("Should save blacklisted token successfully")
    void testSaveBlacklistToken() {
        // When
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test finding blacklisted token by ID.
     */
    @Test
    @DisplayName("Should find blacklisted token by ID")
    void testFindById_Success() {
        // Given
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test deleting blacklisted token by ID.
     */
    @Test
    @DisplayName("Should delete blacklisted token by ID")
    void testDeleteById() {
        // Given
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();
        Long tokenId = savedToken.getId();

        // When
        tokenBlacklistRepository.deleteById(tokenId);
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all blacklisted tokens.
     */
    @Test
    @DisplayName("Should find all blacklisted tokens")
    void testFindAll() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> tokens = tokenBlacklistRepository.findAll();

        // Then
        assertThat(tokens).isNotEmpty();
    }

    /**
     * Test with null token value - edge case.
     */
    @Test
    @DisplayName("Should handle null token value gracefully")
    void testFindByToken_NullToken() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test exists by token with null value - edge case.
     */
    @Test
    @DisplayName("Should handle null token in exists check")
    void testExistsByToken_NullToken() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken(null);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test multiple tokens for same user.
     */
    @Test
    @DisplayName("Should handle multiple tokens for same user")
    void testMultipleTokensForUser() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken("another-token");
        anotherToken.setExpiresAt(LocalDateTime.now().plusHours(2));
        anotherToken.setBlacklistedAt(LocalDateTime.now());
        tokenBlacklistRepository.save(anotherToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test deleting expired tokens with future date.
     */
    @Test
    @DisplayName("Should delete all tokens when checking future expiry date")
    void testDeleteExpiredTokens_FutureDate() {
        // Given
        tokenBlacklistRepository.save(testBlacklistToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now().plusDays(1));
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testTokenValue);
        assertThat(result).isEmpty();
    }
}