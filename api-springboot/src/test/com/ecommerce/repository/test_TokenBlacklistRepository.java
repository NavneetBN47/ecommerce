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
 * Tests repository methods for TokenBlacklist entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TokenBlacklist Repository Tests")
class test_TokenBlacklistRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    private User testUser;
    private TokenBlacklist testBlacklistToken;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test user and blacklisted token.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        entityManager.persist(testUser);

        testBlacklistToken = new TokenBlacklist();
        testBlacklistToken.setToken("test-jwt-token-" + UUID.randomUUID());
        testBlacklistToken.setUser(testUser);
        testBlacklistToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        entityManager.persist(testBlacklistToken);

        entityManager.flush();
    }

    /**
     * Test finding TokenBlacklist by token string.
     * Verifies that the custom query returns the correct blacklisted token.
     */
    @Test
    @DisplayName("Should find TokenBlacklist by token string")
    void testFindByToken_Success() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testBlacklistToken.getToken());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testBlacklistToken.getToken());
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding TokenBlacklist with non-existent token.
     * Verifies that the method returns empty Optional when token doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when token does not exist")
    void testFindByToken_NotFound() {
        // Given
        String nonExistentToken = "non-existent-token";

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(nonExistentToken);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if token exists in blacklist.
     * Verifies that the method returns true for blacklisted tokens.
     */
    @Test
    @DisplayName("Should return true when token exists in blacklist")
    void testExistsByToken_True() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken(testBlacklistToken.getToken());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if non-existent token exists in blacklist.
     * Verifies that the method returns false for non-blacklisted tokens.
     */
    @Test
    @DisplayName("Should return false when token does not exist in blacklist")
    void testExistsByToken_False() {
        // Given
        String nonExistentToken = "non-existent-token";

        // When
        boolean exists = tokenBlacklistRepository.existsByToken(nonExistentToken);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired tokens from blacklist.
     * Verifies that only expired tokens are deleted.
     */
    @Test
    @DisplayName("Should delete expired tokens from blacklist")
    @Transactional
    void testDeleteExpiredTokens_Success() {
        // Given - Create an expired token
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token-" + UUID.randomUUID());
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> expiredResult = tokenBlacklistRepository.findByToken(expiredToken.getToken());
        Optional<TokenBlacklist> validResult = tokenBlacklistRepository.findByToken(testBlacklistToken.getToken());
        
        assertThat(expiredResult).isEmpty();
        assertThat(validResult).isPresent();
    }

    /**
     * Test deleting expired tokens when none exist.
     * Verifies that the operation handles the case when no tokens are expired.
     */
    @Test
    @DisplayName("Should handle delete expired tokens when none exist")
    @Transactional
    void testDeleteExpiredTokens_NoExpired() {
        // Given
        long initialCount = tokenBlacklistRepository.count();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now().minusDays(10));
        entityManager.flush();

        // Then
        long finalCount = tokenBlacklistRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Test finding TokenBlacklist entries by user ID.
     * Verifies that all blacklisted tokens for a user are returned.
     */
    @Test
    @DisplayName("Should find TokenBlacklist entries by user ID")
    void testFindByUserId_Success() {
        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUser.getId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding TokenBlacklist entries with non-existent user ID.
     * Verifies that an empty list is returned when user has no blacklisted tokens.
     */
    @Test
    @DisplayName("Should return empty list when user ID has no blacklisted tokens")
    void testFindByUserId_NotFound() {
        // Given
        Long nonExistentUserId = 99999L;

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new TokenBlacklist entry.
     * Verifies that the repository can persist a new TokenBlacklist entity.
     */
    @Test
    @DisplayName("Should save a new TokenBlacklist entry successfully")
    void testSave_NewToken() {
        // Given
        TokenBlacklist newToken = new TokenBlacklist();
        newToken.setToken("new-jwt-token-" + UUID.randomUUID());
        newToken.setUser(testUser);
        newToken.setExpiresAt(LocalDateTime.now().plusHours(48));

        // When
        TokenBlacklist savedToken = tokenBlacklistRepository.save(newToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(newToken.getToken());
    }

    /**
     * Test updating an existing TokenBlacklist entry.
     * Verifies that the repository can update TokenBlacklist properties.
     */
    @Test
    @DisplayName("Should update existing TokenBlacklist entry successfully")
    void testSave_UpdateToken() {
        // Given
        LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(72);
        testBlacklistToken.setExpiresAt(newExpiryDate);

        // When
        TokenBlacklist updatedToken = tokenBlacklistRepository.save(testBlacklistToken);

        // Then
        assertThat(updatedToken.getExpiresAt()).isEqualToIgnoringNanos(newExpiryDate);
    }

    /**
     * Test finding a TokenBlacklist by ID.
     * Verifies that the repository can retrieve a TokenBlacklist by its primary key.
     */
    @Test
    @DisplayName("Should find TokenBlacklist by ID")
    void testFindById_Success() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(testBlacklistToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testBlacklistToken.getId());
    }

    /**
     * Test deleting a TokenBlacklist entry.
     * Verifies that the repository can delete a TokenBlacklist entity.
     */
    @Test
    @DisplayName("Should delete TokenBlacklist entry successfully")
    void testDelete_Success() {
        // Given
        Long tokenId = testBlacklistToken.getId();

        // When
        tokenBlacklistRepository.delete(testBlacklistToken);
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all TokenBlacklist entries.
     * Verifies that the repository can count the total number of blacklisted tokens.
     */
    @Test
    @DisplayName("Should count all TokenBlacklist entries")
    void testCount_Success() {
        // When
        long count = tokenBlacklistRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if TokenBlacklist exists by ID.
     * Verifies that the repository can check existence of a TokenBlacklist entry.
     */
    @Test
    @DisplayName("Should return true when TokenBlacklist exists")
    void testExistsById_True() {
        // When
        boolean exists = tokenBlacklistRepository.existsById(testBlacklistToken.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if TokenBlacklist exists with non-existent ID.
     * Verifies that the repository returns false for non-existent TokenBlacklist.
     */
    @Test
    @DisplayName("Should return false when TokenBlacklist does not exist")
    void testExistsById_False() {
        // Given
        Long nonExistentId = 99999L;

        // When
        boolean exists = tokenBlacklistRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding multiple blacklisted tokens for the same user.
     * Verifies that a user can have multiple blacklisted tokens.
     */
    @Test
    @DisplayName("Should handle multiple blacklisted tokens for same user")
    void testMultipleTokensPerUser() {
        // Given
        TokenBlacklist token2 = new TokenBlacklist();
        token2.setToken("second-token-" + UUID.randomUUID());
        token2.setUser(testUser);
        token2.setExpiresAt(LocalDateTime.now().plusHours(24));
        entityManager.persist(token2);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(token -> token.getUser().getId().equals(testUser.getId()));
    }

    /**
     * Test token expiry validation.
     * Verifies that tokens can be checked for expiration.
     */
    @Test
    @DisplayName("Should validate token expiry date")
    void testTokenExpiryValidation() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-validation-token-" + UUID.randomUUID());
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        Optional<TokenBlacklist> validToken = tokenBlacklistRepository.findByToken(testBlacklistToken.getToken());
        Optional<TokenBlacklist> expiredTokenResult = tokenBlacklistRepository.findByToken(expiredToken.getToken());

        // Then
        assertThat(validToken).isPresent();
        assertThat(validToken.get().getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(expiredTokenResult).isPresent();
        assertThat(expiredTokenResult.get().getExpiresAt()).isBefore(LocalDateTime.now());
    }

    /**
     * Test finding all TokenBlacklist entries.
     * Verifies that the repository can retrieve all blacklisted tokens.
     */
    @Test
    @DisplayName("Should find all TokenBlacklist entries")
    void testFindAll_Success() {
        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    /**
     * Test deleting multiple expired tokens.
     * Verifies that multiple expired tokens are deleted in one operation.
     */
    @Test
    @DisplayName("Should delete multiple expired tokens")
    @Transactional
    void testDeleteExpiredTokens_Multiple() {
        // Given - Create multiple expired tokens
        TokenBlacklist expiredToken1 = new TokenBlacklist();
        expiredToken1.setToken("expired-1-" + UUID.randomUUID());
        expiredToken1.setUser(testUser);
        expiredToken1.setExpiresAt(LocalDateTime.now().minusHours(2));
        entityManager.persist(expiredToken1);

        TokenBlacklist expiredToken2 = new TokenBlacklist();
        expiredToken2.setToken("expired-2-" + UUID.randomUUID());
        expiredToken2.setUser(testUser);
        expiredToken2.setExpiresAt(LocalDateTime.now().minusHours(3));
        entityManager.persist(expiredToken2);
        entityManager.flush();

        long countBefore = tokenBlacklistRepository.count();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        long countAfter = tokenBlacklistRepository.count();
        assertThat(countAfter).isLessThan(countBefore);
        assertThat(tokenBlacklistRepository.existsByToken(expiredToken1.getToken())).isFalse();
        assertThat(tokenBlacklistRepository.existsByToken(expiredToken2.getToken())).isFalse();
        assertThat(tokenBlacklistRepository.existsByToken(testBlacklistToken.getToken())).isTrue();
    }
}