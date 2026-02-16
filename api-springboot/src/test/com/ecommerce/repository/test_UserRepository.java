package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for UserRepository.
 * Tests repository operations for User entity including custom query methods.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class test_UserRepository {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private String testUsername;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(testUsername);
    }

    /**
     * Test finding a user by username when it exists.
     * Verifies that the custom query method returns the correct user.
     */
    @Test
    @DisplayName("Should find user by username when exists")
    void testFindByUsername_WhenExists_ShouldReturnUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<User> result = userRepository.findByUsername(testUsername);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
    }

    /**
     * Test finding a user by username when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when username does not exist")
    void testFindByUsername_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistentuser");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a user with null username.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null username gracefully")
    void testFindByUsername_WithNullUsername_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByUsername(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a username exists.
     * Verifies that existsByUsername returns true for existing username.
     */
    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_WhenExists_ShouldReturnTrue() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByUsername(testUsername);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a username exists when it does not.
     * Verifies that existsByUsername returns false.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_WhenNotExists_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test checking if username exists with null value.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null username in exists check")
    void testExistsByUsername_WithNullUsername_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername(null);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a user.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save user successfully")
    void testSave_ShouldPersistUser() {
        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }

    /**
     * Test finding a user by ID.
     * Verifies that findById returns the correct user.
     */
    @Test
    @DisplayName("Should find user by ID when exists")
    void testFindById_WhenExists_ShouldReturnUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        UUID savedId = savedUser.getId();

        // When
        Optional<User> result = userRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test finding a user by ID when it does not exist.
     * Verifies that findById returns empty Optional.
     */
    @Test
    @DisplayName("Should return empty when user ID does not exist")
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a user.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete user successfully")
    void testDelete_ShouldRemoveUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        UUID savedId = savedUser.getId();

        // When
        userRepository.delete(savedUser);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all users.
     * Verifies that the count operation returns correct number.
     */
    @Test
    @DisplayName("Should count all users correctly")
    void testCount_ShouldReturnCorrectCount() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Test checking if a user exists by ID.
     * Verifies that existsById returns true for existing user.
     */
    @Test
    @DisplayName("Should return true when user exists by ID")
    void testExistsById_WhenExists_ShouldReturnTrue() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        UUID savedId = savedUser.getId();

        // When
        boolean exists = userRepository.existsById(savedId);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a user exists by ID when it does not.
     * Verifies that existsById returns false.
     */
    @Test
    @DisplayName("Should return false when user does not exist by ID")
    void testExistsById_WhenNotExists_ShouldReturnFalse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = userRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}