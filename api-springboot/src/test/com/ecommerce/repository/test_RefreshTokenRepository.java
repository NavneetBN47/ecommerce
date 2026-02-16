package com.ecommerce.repository;

import com.ecommerce.entity.RefreshToken;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for RefreshTokenRepository.
 * Tests all repository methods including custom query methods for refresh token operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenRepository Tests")
public class test_RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private RefreshToken testToken;
    private String tokenString;

    /**
     * Set up test data before each test method execution.
     * Creates test user and refresh token entities.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        tokenString = UUID.randomUUID().toString();
        testToken = new RefreshToken();
        testToken.setToken(tokenString);
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(testToken);

        entityManager.flush();
    }

    /**
     * Test finding a refresh token by token string.
     * Verifies that the correct refresh token is retrieved.
     */
    @Test
    @DisplayName("Should find refresh token by token string")
    void testFindByToken_Success() {
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenString);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenString);
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding a refresh token with non-existent token string.
     * Verifies that an empty Optional is returned when token doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByToken_NotFound() {
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

        assertThat(result).isEmpty();
    }

    /**
     * Test finding a refresh token with null token string.
     * Verifies proper handling of null token.
     */
    @Test
    @DisplayName("Should handle null token")
    void testFindByToken_NullToken() {
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(null);

        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh tokens by user ID.
     * Verifies that all tokens for a user are successfully deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete refresh tokens by user ID")
    void testDeleteByUserId() {
        refreshTokenRepository.deleteByUserId(testUser.getId());
        entityManager.flush();

        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenString);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting refresh tokens with non-existent user ID.
     * Verifies that delete operation handles non-existent user gracefully.
     */
    @Test
    @Transactional
    @DisplayName("Should handle delete with non-existent user ID")
    void testDeleteByUserId_NotFound() {
        long countBefore = refreshTokenRepository.count();

        refreshTokenRepository.deleteByUserId(999L);
        entityManager.flush();

        long countAfter = refreshTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test deleting expired refresh tokens.
     * Verifies that only expired tokens are deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete expired refresh tokens")
    void testDeleteExpiredTokens() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setUser(testUser);
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        Optional<RefreshToken> expiredResult = refreshTokenRepository.findByToken(expiredToken.getToken());
        Optional<RefreshToken> validResult = refreshTokenRepository.findByToken(tokenString);

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
        long countBefore = refreshTokenRepository.count();

        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now().minusDays(30));
        entityManager.flush();

        long countAfter = refreshTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test saving a new refresh token.
     * Verifies that a refresh token can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new refresh token successfully")
    void testSaveRefreshToken() {
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setUser(testUser);
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        RefreshToken saved = refreshTokenRepository.save(newToken);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isNotNull();
    }

    /**
     * Test updating an existing refresh token.
     * Verifies that refresh token expiry date can be updated.
     */
    @Test
    @DisplayName("Should update existing refresh token")
    void testUpdateRefreshToken() {
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);
        testToken.setExpiryDate(newExpiryDate);
        RefreshToken updated = refreshTokenRepository.save(testToken);

        assertThat(updated.getExpiryDate()).isEqualToIgnoringNanos(newExpiryDate);
        assertThat(updated.getId()).isEqualTo(testToken.getId());
    }

    /**
     * Test finding a refresh token by ID.
     * Verifies that a refresh token can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find refresh token by ID")
    void testFindById_Success() {
        Optional<RefreshToken> result = refreshTokenRepository.findById(testToken.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testToken.getId());
    }

    /**
     * Test deleting a refresh token by ID.
     * Verifies that a refresh token can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete refresh token by ID")
    void testDeleteRefreshToken() {
        Long tokenId = testToken.getId();
        refreshTokenRepository.deleteById(tokenId);
        entityManager.flush();

        Optional<RefreshToken> result = refreshTokenRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all refresh tokens.
     * Verifies that all refresh tokens can be retrieved.
     */
    @Test
    @DisplayName("Should find all refresh tokens")
    void testFindAll() {
        assertThat(refreshTokenRepository.findAll()).isNotEmpty();
        assertThat(refreshTokenRepository.findAll()).hasSize(1);
    }

    /**
     * Test counting refresh tokens.
     * Verifies that the count of refresh tokens is accurate.
     */
    @Test
    @DisplayName("Should count refresh tokens correctly")
    void testCount() {
        long count = refreshTokenRepository.count();

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
            RefreshToken expiredToken = new RefreshToken();
            expiredToken.setToken(UUID.randomUUID().toString());
            expiredToken.setUser(testUser);
            expiredToken.setExpiryDate(LocalDateTime.now().minusDays(i + 1));
            entityManager.persist(expiredToken);
        }
        entityManager.flush();

        long countBefore = refreshTokenRepository.count();
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();
        long countAfter = refreshTokenRepository.count();

        assertThat(countBefore).isEqualTo(4L);
        assertThat(countAfter).isEqualTo(1L);
    }
}