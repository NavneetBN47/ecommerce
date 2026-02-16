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
 * Tests repository methods for RefreshToken entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshToken Repository Tests")
class test_RefreshTokenRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private RefreshToken testToken;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test user and refresh token.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        entityManager.persist(testUser);

        testToken = new RefreshToken();
        testToken.setToken(UUID.randomUUID().toString());
        testToken.setUserId(testUser.getId());
        testToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(testToken);

        entityManager.flush();
    }

    /**
     * Test finding RefreshToken by token string.
     * Verifies that the custom query returns the correct token.
     */
    @Test
    @DisplayName("Should find RefreshToken by token string")
    void testFindByToken_Success() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken.getToken());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken.getToken());
        assertThat(result.get().getUserId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding RefreshToken with non-existent token.
     * Verifies that the method returns empty Optional when token doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when token does not exist")
    void testFindByToken_NotFound() {
        // Given
        String nonExistentToken = UUID.randomUUID().toString();

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(nonExistentToken);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting RefreshToken by user ID.
     * Verifies that all tokens for a user are deleted.
     */
    @Test
    @DisplayName("Should delete RefreshToken by user ID")
    @Transactional
    void testDeleteByUserId_Success() {
        // Given
        Long userId = testUser.getId();

        // When
        refreshTokenRepository.deleteByUserId(userId);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(testToken.getToken());
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting RefreshToken with non-existent user ID.
     * Verifies that the delete operation handles non-existent user gracefully.
     */
    @Test
    @DisplayName("Should handle delete by non-existent user ID gracefully")
    @Transactional
    void testDeleteByUserId_NotFound() {
        // Given
        Long nonExistentUserId = 99999L;
        long initialCount = refreshTokenRepository.count();

        // When
        refreshTokenRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();

        // Then
        long finalCount = refreshTokenRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Test deleting expired tokens.
     * Verifies that only expired tokens are deleted.
     */
    @Test
    @DisplayName("Should delete expired tokens")
    @Transactional
    void testDeleteExpiredTokens_Success() {
        // Given - Create an expired token
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setUserId(testUser.getId());
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        Optional<RefreshToken> expiredResult = refreshTokenRepository.findByToken(expiredToken.getToken());
        Optional<RefreshToken> validResult = refreshTokenRepository.findByToken(testToken.getToken());
        
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
        long initialCount = refreshTokenRepository.count();

        // When
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now().minusDays(10));
        entityManager.flush();

        // Then
        long finalCount = refreshTokenRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Test saving a new RefreshToken.
     * Verifies that the repository can persist a new RefreshToken entity.
     */
    @Test
    @DisplayName("Should save a new RefreshToken successfully")
    void testSave_NewToken() {
        // Given
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setUserId(testUser.getId());
        newToken.setExpiryDate(LocalDateTime.now().plusDays(14));

        // When
        RefreshToken savedToken = refreshTokenRepository.save(newToken);

        // Then
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(newToken.getToken());
    }

    /**
     * Test updating an existing RefreshToken.
     * Verifies that the repository can update RefreshToken properties.
     */
    @Test
    @DisplayName("Should update existing RefreshToken successfully")
    void testSave_UpdateToken() {
        // Given
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(30);
        testToken.setExpiryDate(newExpiryDate);

        // When
        RefreshToken updatedToken = refreshTokenRepository.save(testToken);

        // Then
        assertThat(updatedToken.getExpiryDate()).isEqualToIgnoringNanos(newExpiryDate);
    }

    /**
     * Test finding a RefreshToken by ID.
     * Verifies that the repository can retrieve a RefreshToken by its primary key.
     */
    @Test
    @DisplayName("Should find RefreshToken by ID")
    void testFindById_Success() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findById(testToken.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testToken.getId());
    }

    /**
     * Test deleting a RefreshToken.
     * Verifies that the repository can delete a RefreshToken entity.
     */
    @Test
    @DisplayName("Should delete RefreshToken successfully")
    void testDelete_Success() {
        // Given
        Long tokenId = testToken.getId();

        // When
        refreshTokenRepository.delete(testToken);
        entityManager.flush();

        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(tokenId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all RefreshTokens.
     * Verifies that the repository can count the total number of RefreshTokens.
     */
    @Test
    @DisplayName("Should count all RefreshTokens")
    void testCount_Success() {
        // When
        long count = refreshTokenRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if RefreshToken exists by ID.
     * Verifies that the repository can check existence of a RefreshToken.
     */
    @Test
    @DisplayName("Should return true when RefreshToken exists")
    void testExistsById_True() {
        // When
        boolean exists = refreshTokenRepository.existsById(testToken.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if RefreshToken exists with non-existent ID.
     * Verifies that the repository returns false for non-existent RefreshToken.
     */
    @Test
    @DisplayName("Should return false when RefreshToken does not exist")
    void testExistsById_False() {
        // Given
        Long nonExistentId = 99999L;

        // When
        boolean exists = refreshTokenRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding multiple tokens for the same user.
     * Verifies that a user can have multiple refresh tokens.
     */
    @Test
    @DisplayName("Should handle multiple tokens for same user")
    void testMultipleTokensPerUser() {
        // Given
        RefreshToken token2 = new RefreshToken();
        token2.setToken(UUID.randomUUID().toString());
        token2.setUserId(testUser.getId());
        token2.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(token2);
        entityManager.flush();

        // When
        Optional<RefreshToken> result1 = refreshTokenRepository.findByToken(testToken.getToken());
        Optional<RefreshToken> result2 = refreshTokenRepository.findByToken(token2.getToken());

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().getUserId()).isEqualTo(result2.get().getUserId());
    }

    /**
     * Test token expiry date validation.
     * Verifies that tokens can be checked for expiration.
     */
    @Test
    @DisplayName("Should validate token expiry date")
    void testTokenExpiryValidation() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setUserId(testUser.getId());
        expiredToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        entityManager.persist(expiredToken);
        entityManager.flush();

        // When
        Optional<RefreshToken> validToken = refreshTokenRepository.findByToken(testToken.getToken());
        Optional<RefreshToken> expiredTokenResult = refreshTokenRepository.findByToken(expiredToken.getToken());

        // Then
        assertThat(validToken).isPresent();
        assertThat(validToken.get().getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(expiredTokenResult).isPresent();
        assertThat(expiredTokenResult.get().getExpiryDate()).isBefore(LocalDateTime.now());
    }

    /**
     * Test deleting all tokens for a user with multiple tokens.
     * Verifies that deleteByUserId removes all tokens for the user.
     */
    @Test
    @DisplayName("Should delete all tokens for user with multiple tokens")
    @Transactional
    void testDeleteByUserId_MultipleTokens() {
        // Given
        RefreshToken token2 = new RefreshToken();
        token2.setToken(UUID.randomUUID().toString());
        token2.setUserId(testUser.getId());
        token2.setExpiryDate(LocalDateTime.now().plusDays(7));
        entityManager.persist(token2);
        entityManager.flush();

        // When
        refreshTokenRepository.deleteByUserId(testUser.getId());
        entityManager.flush();

        // Then
        Optional<RefreshToken> result1 = refreshTokenRepository.findByToken(testToken.getToken());
        Optional<RefreshToken> result2 = refreshTokenRepository.findByToken(token2.getToken());
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
    }
}