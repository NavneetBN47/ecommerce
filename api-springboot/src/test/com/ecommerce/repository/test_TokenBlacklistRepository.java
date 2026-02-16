package com.ecommerce.repository;

import com.ecommerce.entity.TokenBlacklist;
import com.ecommerce.entity.User;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for TokenBlacklistRepository.
 * Tests all repository methods including custom query methods for token blacklist operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TokenBlacklistRepository Tests")
public class test_TokenBlacklistRepository {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private TokenBlacklist testBlacklistedToken;
    private String tokenString;

    /**
     * Set up test data before each test method execution.
     * Creates test user and blacklisted token entities.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        tokenString = "test-token-" + UUID.randomUUID().toString();
        testBlacklistedToken = new TokenBlacklist();
        testBlacklistedToken.setToken(tokenString);
        testBlacklistedToken.setUser(testUser);
        testBlacklistedToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        testBlacklistedToken.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(testBlacklistedToken);

        entityManager.flush();
    }

    /**
     * Test finding a blacklisted token by token string.
     * Verifies that the correct blacklisted token is retrieved.
     */
    @Test
    @DisplayName("Should find blacklisted token by token string")
    void testFindByToken_Success() {
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(tokenString);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenString);
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding a blacklisted token with non-existent token string.
     * Verifies that an empty Optional is returned when token doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByToken_NotFound() {
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("non-existent-token");

        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a token exists in blacklist.
     * Verifies that existence check returns true for blacklisted token.
     */
    @Test
    @DisplayName("Should return true when token exists in blacklist")
    void testExistsByToken_Exists() {
        boolean exists = tokenBlacklistRepository.existsByToken(tokenString);

        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a non-existent token exists in blacklist.
     * Verifies that existence check returns false for non-blacklisted token.
     */
    @Test
    @DisplayName("Should return false when token does not exist in blacklist")
    void testExistsByToken_NotExists() {
        boolean exists = tokenBlacklistRepository.existsByToken("non-existent-token");

        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired blacklisted tokens.
     * Verifies that only expired tokens are deleted from blacklist.
     */
    @Test
    @Transactional
    @DisplayName("Should delete expired blacklisted tokens")
    void testDeleteExpiredTokens() {
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token");
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        expiredToken.setBlacklistedAt(LocalDateTime.now().minusDays(2));
        entityManager.persist(expiredToken);
        entityManager.flush();

        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        Optional<TokenBlacklist> expiredResult = tokenBlacklistRepository.findByToken("expired-token");
        Optional<TokenBlacklist> validResult = tokenBlacklistRepository.findByToken(tokenString);

        assertThat(expiredResult).isEmpty();
        assertThat(validResult).isPresent();
    }

    /**
     * Test deleting expired tokens when none exist.
     * Verifies that delete operation handles case with no expired tokens.
     */
    @Test
    @Transactional
    @DisplayName("Should handle delete when no expired tokens exist")
    void testDeleteExpiredTokens_NoExpiredTokens() {
        long countBefore = tokenBlacklistRepository.count();

        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now().minusDays(30));
        entityManager.flush();

        long countAfter = tokenBlacklistRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test finding blacklisted tokens by user ID.
     * Verifies that all blacklisted tokens for a user are retrieved.
     */
    @Test
    @DisplayName("Should find blacklisted tokens by user ID")
    void testFindByUserId_Success() {
        List<TokenBlacklist> results = tokenBlacklistRepository.findByUserId(testUser.getId());

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding blacklisted tokens with non-existent user ID.
     * Verifies that an empty list is returned when user ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty list when user ID not found")
    void testFindByUserId_NotFound() {
        List<TokenBlacklist> results = tokenBlacklistRepository.findByUserId(999L);

        assertThat(results).isEmpty();
    }

    /**
     * Test finding multiple blacklisted tokens for same user.
     * Verifies that all tokens for a user are retrieved.
     */
    @Test
    @DisplayName("Should find multiple blacklisted tokens for same user")
    void testFindByUserId_MultipleTokens() {
        TokenBlacklist token2 = new TokenBlacklist();
        token2.setToken("token-2");
        token2.setUser(testUser);
        token2.setExpiresAt(LocalDateTime.now().plusDays(1));
        token2.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(token2);
        entityManager.flush();

        List<TokenBlacklist> results = tokenBlacklistRepository.findByUserId(testUser.getId());

        assertThat(results).hasSize(2);
    }

    /**
     * Test saving a new blacklisted token.
     * Verifies that a token can be successfully added to blacklist.
     */
    @Test
    @DisplayName("Should save new blacklisted token successfully")
    void testSaveBlacklistedToken() {
        TokenBlacklist newToken = new TokenBlacklist();
        newToken.setToken("new-token");
        newToken.setUser(testUser);
        newToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        newToken.setBlacklistedAt(LocalDateTime.now());

        TokenBlacklist saved = tokenBlacklistRepository.save(newToken);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo("new-token");
    }

    /**
     * Test updating an existing blacklisted token.
     * Verifies that blacklisted token expiry date can be updated.
     */
    @Test
    @DisplayName("Should update existing blacklisted token")
    void testUpdateBlacklistedToken() {
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(7);
        testBlacklistedToken.setExpiresAt(newExpiryDate);
        TokenBlacklist updated = tokenBlacklistRepository.save(testBlacklistedToken);

        assertThat(updated.getExpiresAt()).isEqualToIgnoringNanos(newExpiryDate);
        assertThat(updated.getId()).isEqualTo(testBlacklistedToken.getId());
    }

    /**
     * Test finding a blacklisted token by ID.
     * Verifies that a blacklisted token can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find blacklisted token by ID")
    void testFindById_Success() {
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(testBlacklistedToken.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testBlacklistedToken.getId());
    }

    /**
     * Test deleting a blacklisted token by ID.
     * Verifies that a blacklisted token can be successfully removed.
     */
    @Test
    @DisplayName("Should delete blacklisted token by ID")
    void testDeleteBlacklistedToken() {
        Long tokenId = testBlacklistedToken.getId();
        tokenBlacklistRepository.deleteById(tokenId);
        entityManager.flush();

        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all blacklisted tokens.
     * Verifies that all blacklisted tokens can be retrieved.
     */
    @Test
    @DisplayName("Should find all blacklisted tokens")
    void testFindAll() {
        assertThat(tokenBlacklistRepository.findAll()).isNotEmpty();
        assertThat(tokenBlacklistRepository.findAll()).hasSize(1);
    }

    /**
     * Test counting blacklisted tokens.
     * Verifies that the count of blacklisted tokens is accurate.
     */
    @Test
    @DisplayName("Should count blacklisted tokens correctly")
    void testCount() {
        long count = tokenBlacklistRepository.count();

        assertThat(count).isEqualTo(1L);
    }

    /**
     * Test deleting multiple expired tokens.
     * Verifies that multiple expired tokens are deleted correctly.
     */
    @Test
    @Transactional
    @DisplayName("Should delete multiple expired tokens")
    void testDeleteExpiredTokens_MultipleTokens() {
        for (int i = 0; i < 3; i++) {
            TokenBlacklist expiredToken = new TokenBlacklist();
            expiredToken.setToken("expired-" + i);
            expiredToken.setUser(testUser);
            expiredToken.setExpiresAt(LocalDateTime.now().minusDays(i + 1));
            expiredToken.setBlacklistedAt(LocalDateTime.now().minusDays(i + 2));
            entityManager.persist(expiredToken);
        }
        entityManager.flush();

        long countBefore = tokenBlacklistRepository.count();
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();
        long countAfter = tokenBlacklistRepository.count();

        assertThat(countBefore).isEqualTo(4L);
        assertThat(countAfter).isEqualTo(1L);
    }

    /**
     * Test checking existence with null token.
     * Verifies proper handling of null token in existence check.
     */
    @Test
    @DisplayName("Should handle null token in existence check")
    void testExistsByToken_NullToken() {
        boolean exists = tokenBlacklistRepository.existsByToken(null);

        assertThat(exists).isFalse();
    }
}