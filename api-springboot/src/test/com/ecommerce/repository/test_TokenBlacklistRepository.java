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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
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
    private String testTokenValue;
    private Long testUserId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testTokenValue = "blacklisted-token-12345";
        testUserId = 1L;
        
        testBlacklistToken = new TokenBlacklist();
        testBlacklistToken.setToken(testTokenValue);
        testBlacklistToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        testBlacklistToken.setBlacklistedAt(LocalDateTime.now());
    }

    /**
     * Test finding blacklisted token by token value when token exists.
     * Verifies that the correct token is returned.
     */
    @Test
    @DisplayName("Should find blacklisted token by token value when exists")
    void testFindByToken_WhenExists() {
        // Given
        entityManager.persist(testBlacklistToken);
        entityManager.flush();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(testTokenValue);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test finding blacklisted token by token value when token does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token not found")
    void testFindByToken_WhenNotExists() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if token exists in blacklist.
     * Verifies that the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when token exists in blacklist")
    void testExistsByToken_WhenExists() {
        // Given
        entityManager.persist(testBlacklistToken);
        entityManager.flush();

        // When
        boolean exists = tokenBlacklistRepository.existsByToken(testTokenValue);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if token exists in blacklist when it doesn't.
     * Verifies that the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when token does not exist in blacklist")
    void testExistsByToken_WhenNotExists() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken("non-existent-token");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired tokens from blacklist.
     * Verifies that only expired tokens are removed.
     */
    @Test
    @DisplayName("Should delete expired tokens from blacklist")
    void testDeleteExpiredTokens() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setBlacklistedAt(LocalDateTime.now().minusHours(2));
        entityManager.persist(expiredToken);
        
        TokenBlacklist validToken = new TokenBlacklist();
        validToken.setToken("valid-token");
        validToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        validToken.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(validToken);
        entityManager.flush();
        entityManager.clear();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> expiredResult = tokenBlacklistRepository.findByToken("expired-token");
        Optional<TokenBlacklist> validResult = tokenBlacklistRepository.findByToken("valid-token");
        
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
        entityManager.persist(testBlacklistToken);
        entityManager.flush();
        long countBefore = tokenBlacklistRepository.count();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        long countAfter = tokenBlacklistRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test finding blacklisted tokens by user ID.
     * Verifies that all tokens for the specified user are returned.
     */
    @Test
    @DisplayName("Should find blacklisted tokens by user ID")
    void testFindByUserId() {
        // Given
        testBlacklistToken.setUser(createMockUser(testUserId));
        entityManager.persist(testBlacklistToken);
        
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken("another-token");
        anotherToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        anotherToken.setBlacklistedAt(LocalDateTime.now());
        anotherToken.setUser(createMockUser(testUserId));
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(token -> token.getUser().getUserId().equals(testUserId));
    }

    /**
     * Test finding blacklisted tokens by user ID when user has no tokens.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when user has no blacklisted tokens")
    void testFindByUserId_WhenNoTokens() {
        // Given
        Long nonExistentUserId = 999L;

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new blacklisted token.
     * Verifies that the token is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new blacklisted token successfully")
    void testSave_NewToken() {
        // When
        TokenBlacklist savedToken = tokenBlacklistRepository.save(testBlacklistToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(testTokenValue);
    }

    /**
     * Test updating an existing blacklisted token.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing blacklisted token successfully")
    void testSave_UpdateToken() {
        // Given
        TokenBlacklist savedToken = entityManager.persist(testBlacklistToken);
        entityManager.flush();
        Long savedId = savedToken.getId();
        LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(2);

        // When
        savedToken.setExpiresAt(newExpiryDate);
        TokenBlacklist updatedToken = tokenBlacklistRepository.save(savedToken);

        // Then
        assertThat(updatedToken.getId()).isEqualTo(savedId);
        assertThat(updatedToken.getExpiresAt()).isEqualToIgnoringNanos(newExpiryDate);
    }

    /**
     * Test finding blacklisted token by ID.
     * Verifies that the correct token is retrieved.
     */
    @Test
    @DisplayName("Should find blacklisted token by ID when exists")
    void testFindById_WhenExists() {
        // Given
        TokenBlacklist savedToken = entityManager.persist(testBlacklistToken);
        entityManager.flush();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedToken.getId());
    }

    /**
     * Test finding blacklisted token by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a blacklisted token by ID.
     * Verifies that the token is removed from the database.
     */
    @Test
    @DisplayName("Should delete blacklisted token by ID successfully")
    void testDeleteById() {
        // Given
        TokenBlacklist savedToken = entityManager.persist(testBlacklistToken);
        entityManager.flush();
        Long savedId = savedToken.getId();

        // When
        tokenBlacklistRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all blacklisted tokens.
     * Verifies that all persisted tokens are retrieved.
     */
    @Test
    @DisplayName("Should find all blacklisted tokens")
    void testFindAll() {
        // Given
        entityManager.persist(testBlacklistToken);
        
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken("another-token");
        anotherToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        anotherToken.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> allTokens = tokenBlacklistRepository.findAll();

        // Then
        assertThat(allTokens).hasSize(2);
    }

    /**
     * Test checking if blacklisted token exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when token exists by ID")
    void testExistsById_WhenExists() {
        // Given
        TokenBlacklist savedToken = entityManager.persist(testBlacklistToken);
        entityManager.flush();

        // When
        boolean exists = tokenBlacklistRepository.existsById(savedToken.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if blacklisted token exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when token does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = tokenBlacklistRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all blacklisted tokens.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all blacklisted tokens correctly")
    void testCount() {
        // Given
        entityManager.persist(testBlacklistToken);
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken("another-token");
        anotherToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        anotherToken.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        long count = tokenBlacklistRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    /**
     * Helper method to create a mock user for testing.
     */
    private com.ecommerce.entity.User createMockUser(Long userId) {
        com.ecommerce.entity.User user = new com.ecommerce.entity.User();
        user.setUserId(userId);
        return entityManager.persist(user);
    }
}