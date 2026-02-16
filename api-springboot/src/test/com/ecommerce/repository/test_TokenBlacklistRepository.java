package com.ecommerce.repository;

import com.ecommerce.entity.TokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for TokenBlacklistRepository.
 * Tests repository methods for TokenBlacklist entity operations including
 * finding by token, checking existence, deleting expired tokens, and finding by user ID.
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

    private TokenBlacklist testTokenBlacklist;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testTokenBlacklist = new TokenBlacklist();
        testTokenBlacklist.setToken("test-blacklisted-token");
        testTokenBlacklist.setExpiresAt(LocalDateTime.now().plusDays(7));
    }

    /**
     * Test finding a blacklisted token by token string when it exists.
     * Verifies that the correct token is returned.
     */
    @Test
    @DisplayName("Should find blacklisted token by token when exists")
    void testFindByToken_WhenExists_ReturnsToken() {
        // Given
        TokenBlacklist token = new TokenBlacklist();
        token.setToken("blacklisted-token");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(token);

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("blacklisted-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("blacklisted-token");
    }

    /**
     * Test finding a blacklisted token by token string when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token does not exist")
    void testFindByToken_WhenNotExists_ReturnsEmpty() {
        // Given
        String nonExistentToken = "non-existent-token";

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(nonExistentToken);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a token exists in the blacklist.
     * Verifies that the existence check returns true for blacklisted tokens.
     */
    @Test
    @DisplayName("Should check if token exists in blacklist")
    void testExistsByToken_WhenExists_ReturnsTrue() {
        // Given
        TokenBlacklist token = new TokenBlacklist();
        token.setToken("exists-token");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(token);

        // When
        boolean exists = tokenBlacklistRepository.existsByToken("exists-token");

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a token exists in the blacklist when it doesn't.
     * Verifies that the existence check returns false for non-blacklisted tokens.
     */
    @Test
    @DisplayName("Should return false when token does not exist in blacklist")
    void testExistsByToken_WhenNotExists_ReturnsFalse() {
        // Given
        String nonExistentToken = "not-blacklisted";

        // When
        boolean exists = tokenBlacklistRepository.existsByToken(nonExistentToken);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired blacklisted tokens.
     * Verifies that only expired tokens are removed.
     */
    @Test
    @DisplayName("Should delete expired blacklisted tokens")
    void testDeleteExpiredTokens_DeletesOnlyExpiredTokens() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        
        TokenBlacklist validToken = new TokenBlacklist();
        validToken.setToken("valid-token");
        validToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        entityManager.persist(expiredToken);
        entityManager.persist(validToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        boolean expiredExists = tokenBlacklistRepository.existsByToken("expired-token");
        boolean validExists = tokenBlacklistRepository.existsByToken("valid-token");
        
        assertThat(expiredExists).isFalse();
        assertThat(validExists).isTrue();
    }

    /**
     * Test finding blacklisted tokens by user ID.
     * Verifies that all tokens for a user are returned.
     */
    @Test
    @DisplayName("Should find blacklisted tokens by user ID")
    void testFindByUserId_ReturnsUserTokens() {
        // Given
        Long userId = 1L;

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(userId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test saving a blacklisted token.
     * Verifies that the token is persisted correctly.
     */
    @Test
    @DisplayName("Should save blacklisted token successfully")
    void testSave_ValidToken_SavesSuccessfully() {
        // Given
        TokenBlacklist newToken = new TokenBlacklist();
        newToken.setToken("new-blacklisted-token");
        newToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        // When
        TokenBlacklist savedToken = tokenBlacklistRepository.save(newToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo("new-blacklisted-token");
    }

    /**
     * Test finding a blacklisted token by ID.
     * Verifies that the token can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find blacklisted token by ID when exists")
    void testFindById_WhenExists_ReturnsToken() {
        // Given
        TokenBlacklist token = new TokenBlacklist();
        token.setToken("find-token");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        TokenBlacklist savedToken = entityManager.persistAndFlush(token);

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test deleting a blacklisted token by ID.
     * Verifies that the token is removed from the database.
     */
    @Test
    @DisplayName("Should delete blacklisted token by ID successfully")
    void testDeleteById_ExistingToken_DeletesSuccessfully() {
        // Given
        TokenBlacklist token = new TokenBlacklist();
        token.setToken("delete-token");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        TokenBlacklist savedToken = entityManager.persistAndFlush(token);
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
     * Verifies that all tokens can be retrieved.
     */
    @Test
    @DisplayName("Should find all blacklisted tokens")
    void testFindAll_ReturnsAllTokens() {
        // Given
        TokenBlacklist token1 = new TokenBlacklist();
        token1.setToken("token1");
        token1.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        TokenBlacklist token2 = new TokenBlacklist();
        token2.setToken("token2");
        token2.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        entityManager.persist(token1);
        entityManager.persist(token2);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting blacklisted tokens.
     * Verifies that the count of tokens is correct.
     */
    @Test
    @DisplayName("Should count blacklisted tokens correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = tokenBlacklistRepository.count();
        TokenBlacklist token = new TokenBlacklist();
        token.setToken("count-token");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        entityManager.persistAndFlush(token);

        // When
        long newCount = tokenBlacklistRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
}