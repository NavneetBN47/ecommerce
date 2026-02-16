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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for TokenBlacklistRepository.
 * Tests repository methods for TokenBlacklist entity operations including token blacklist management.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
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
     * Creates and persists test User and TokenBlacklist entities.
     */
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        // Create test blacklisted token
        tokenString = "test-jwt-token-" + UUID.randomUUID().toString();
        testBlacklistedToken = new TokenBlacklist();
        testBlacklistedToken.setToken(tokenString);
        testBlacklistedToken.setUser(testUser);
        testBlacklistedToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        testBlacklistedToken.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(testBlacklistedToken);
        
        entityManager.flush();
    }

    /**
     * Test finding a TokenBlacklist entry by token string when it exists.
     * Verifies that the correct TokenBlacklist entry is returned.
     */
    @Test
    @DisplayName("Should find TokenBlacklist by token string when exists")
    void testFindByToken_WhenExists_ReturnsToken() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(tokenString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenString);
        assertThat(result.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    /**
     * Test finding a TokenBlacklist entry by token string when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when token is not blacklisted")
    void testFindByToken_WhenNotExists_ReturnsEmpty() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken("nonexistent-token");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a token exists in the blacklist.
     * Verifies that the method returns true for blacklisted tokens.
     */
    @Test
    @DisplayName("Should return true when token exists in blacklist")
    void testExistsByToken_WhenExists_ReturnsTrue() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken(tokenString);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a token exists in the blacklist when it does not.
     * Verifies that the method returns false for non-blacklisted tokens.
     */
    @Test
    @DisplayName("Should return false when token does not exist in blacklist")
    void testExistsByToken_WhenNotExists_ReturnsFalse() {
        // When
        boolean exists = tokenBlacklistRepository.existsByToken("nonexistent-token");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test deleting expired tokens from blacklist.
     * Verifies that only expired tokens are removed.
     */
    @Test
    @DisplayName("Should delete expired tokens from blacklist")
    void testDeleteExpiredTokens_RemovesExpiredOnly() {
        // Given
        String expiredTokenString = "expired-token-" + UUID.randomUUID().toString();
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken(expiredTokenString);
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setBlacklistedAt(LocalDateTime.now().minusHours(2));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> validToken = tokenBlacklistRepository.findByToken(tokenString);
        Optional<TokenBlacklist> removedToken = tokenBlacklistRepository.findByToken(expiredTokenString);
        
        assertThat(validToken).isPresent();
        assertThat(removedToken).isEmpty();
    }

    /**
     * Test deleting expired tokens when none are expired.
     * Verifies that valid tokens are not removed.
     */
    @Test
    @DisplayName("Should not delete valid tokens when deleting expired")
    void testDeleteExpiredTokens_WhenNoneExpired_KeepsAllTokens() {
        // When
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(tokenString);
        assertThat(result).isPresent();
    }

    /**
     * Test finding all blacklisted tokens for a user.
     * Verifies that all tokens for the specified user are returned.
     */
    @Test
    @DisplayName("Should find all blacklisted tokens by user ID")
    void testFindByUserId_ReturnsAllUserTokens() {
        // Given
        String anotherTokenString = "another-token-" + UUID.randomUUID().toString();
        TokenBlacklist anotherToken = new TokenBlacklist();
        anotherToken.setToken(anotherTokenString);
        anotherToken.setUser(testUser);
        anotherToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        anotherToken.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(anotherToken);
        entityManager.flush();

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(testUser.getUserId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TokenBlacklist::getToken)
            .containsExactlyInAnyOrder(tokenString, anotherTokenString);
    }

    /**
     * Test finding blacklisted tokens by user ID when user has no tokens.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when user has no blacklisted tokens")
    void testFindByUserId_WhenNoTokens_ReturnsEmpty() {
        // Given
        Long nonExistentUserId = 99999L;

        // When
        List<TokenBlacklist> result = tokenBlacklistRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new TokenBlacklist entry.
     * Verifies that the TokenBlacklist entry is persisted correctly.
     */
    @Test
    @DisplayName("Should save new TokenBlacklist entry successfully")
    void testSave_NewBlacklistEntry_Success() {
        // Given
        String newTokenString = "new-token-" + UUID.randomUUID().toString();
        TokenBlacklist newBlacklistEntry = new TokenBlacklist();
        newBlacklistEntry.setToken(newTokenString);
        newBlacklistEntry.setUser(testUser);
        newBlacklistEntry.setExpiresAt(LocalDateTime.now().plusHours(24));
        newBlacklistEntry.setBlacklistedAt(LocalDateTime.now());

        // When
        TokenBlacklist savedEntry = tokenBlacklistRepository.save(newBlacklistEntry);

        // Then
        assertThat(savedEntry).isNotNull();
        assertThat(savedEntry.getId()).isNotNull();
        assertThat(savedEntry.getToken()).isEqualTo(newTokenString);
    }

    /**
     * Test updating an existing TokenBlacklist entry.
     * Verifies that TokenBlacklist modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing TokenBlacklist entry successfully")
    void testSave_UpdateBlacklistEntry_Success() {
        // Given
        LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(48);
        testBlacklistedToken.setExpiresAt(newExpiryDate);

        // When
        TokenBlacklist updatedEntry = tokenBlacklistRepository.save(testBlacklistedToken);
        entityManager.flush();

        // Then
        assertThat(updatedEntry.getExpiresAt()).isEqualToIgnoringNanos(newExpiryDate);
    }

    /**
     * Test deleting a TokenBlacklist entry by ID.
     * Verifies that the entry is removed from the database.
     */
    @Test
    @DisplayName("Should delete TokenBlacklist entry by ID successfully")
    void testDeleteById_Success() {
        // Given
        Long entryId = testBlacklistedToken.getId();

        // When
        tokenBlacklistRepository.deleteById(entryId);
        entityManager.flush();

        // Then
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(entryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a TokenBlacklist entry by ID.
     * Verifies that the correct entry is retrieved.
     */
    @Test
    @DisplayName("Should find TokenBlacklist entry by ID when exists")
    void testFindById_WhenExists_ReturnsEntry() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findById(testBlacklistedToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testBlacklistedToken.getId());
        assertThat(result.get().getToken()).isEqualTo(tokenString);
    }

    /**
     * Test that blacklisted tokens maintain relationship with User.
     * Verifies user-token relationship integrity.
     */
    @Test
    @DisplayName("Should maintain relationship with User")
    void testBlacklistUserRelationship() {
        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(tokenString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isNotNull();
        assertThat(result.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    /**
     * Test finding all TokenBlacklist entries.
     * Verifies that all persisted entries are retrieved.
     */
    @Test
    @DisplayName("Should find all TokenBlacklist entries")
    void testFindAll_ReturnsAllEntries() {
        // When
        List<TokenBlacklist> allEntries = tokenBlacklistRepository.findAll();

        // Then
        assertThat(allEntries).isNotEmpty();
        assertThat(allEntries).hasSize(1);
    }

    /**
     * Test blacklisting multiple tokens for the same user.
     * Verifies that multiple tokens can be blacklisted per user.
     */
    @Test
    @DisplayName("Should allow multiple blacklisted tokens per user")
    void testMultipleBlacklistedTokensPerUser() {
        // Given
        TokenBlacklist token2 = new TokenBlacklist();
        token2.setToken("token2-" + UUID.randomUUID().toString());
        token2.setUser(testUser);
        token2.setExpiresAt(LocalDateTime.now().plusHours(24));
        token2.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(token2);

        TokenBlacklist token3 = new TokenBlacklist();
        token3.setToken("token3-" + UUID.randomUUID().toString());
        token3.setUser(testUser);
        token3.setExpiresAt(LocalDateTime.now().plusHours(24));
        token3.setBlacklistedAt(LocalDateTime.now());
        entityManager.persist(token3);
        entityManager.flush();

        // When
        List<TokenBlacklist> userTokens = tokenBlacklistRepository.findByUserId(testUser.getUserId());

        // Then
        assertThat(userTokens).hasSize(3);
        assertThat(userTokens).allMatch(t -> t.getUser().getUserId().equals(testUser.getUserId()));
    }

    /**
     * Test token expiry validation.
     * Verifies that tokens with past expiry dates are identified correctly.
     */
    @Test
    @DisplayName("Should correctly identify expired blacklisted tokens")
    void testExpiredTokenIdentification() {
        // Given
        TokenBlacklist expiredToken = new TokenBlacklist();
        expiredToken.setToken("expired-" + UUID.randomUUID().toString());
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        expiredToken.setBlacklistedAt(LocalDateTime.now().minusHours(2));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        Optional<TokenBlacklist> result = tokenBlacklistRepository.findByToken(expiredToken.getToken());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getExpiresAt()).isBefore(LocalDateTime.now());
    }
}