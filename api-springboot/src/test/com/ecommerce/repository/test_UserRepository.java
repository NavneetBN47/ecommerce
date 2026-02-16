package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for UserRepository.
 * Tests repository methods for User entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("UserRepository Tests")
public class test_UserRepository {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private String testUsername;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(testUsername);
        testUser.setEmail("testuser@example.com");
    }

    /**
     * Test finding a user by username when it exists.
     * Verifies that the repository correctly retrieves a user by username.
     */
    @Test
    @DisplayName("Should find user by username when exists")
    void testFindByUsername_WhenExists() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> result = userRepository.findByUsername(testUsername);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
    }

    /**
     * Test finding a user by username when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent username.
     */
    @Test
    @DisplayName("Should return empty when username does not exist")
    void testFindByUsername_WhenNotExists() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistentuser");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if username exists.
     * Verifies that the repository correctly checks username existence.
     */
    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername_WhenExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByUsername(testUsername);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if username does not exist.
     * Verifies that the repository correctly returns false for non-existent username.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_WhenNotExists() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a user.
     * Verifies that the repository correctly persists a user.
     */
    @Test
    @DisplayName("Should save user successfully")
    void testSave_User() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUsername);
    }

    /**
     * Test finding a user by ID.
     * Verifies that the repository correctly retrieves a user by its ID.
     */
    @Test
    @DisplayName("Should find user by ID")
    void testFindById_WhenExists() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> result = userRepository.findById(savedUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    /**
     * Test finding a user by ID when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent user.
     */
    @Test
    @DisplayName("Should return empty when user ID does not exist")
    void testFindById_WhenNotExists() {
        // When
        Optional<User> result = userRepository.findById(UUID.randomUUID());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a user.
     * Verifies that the repository correctly deletes a user.
     */
    @Test
    @DisplayName("Should delete user successfully")
    void testDelete_User() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        UUID userId = savedUser.getId();

        // When
        userRepository.delete(savedUser);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all users.
     * Verifies that the repository correctly retrieves all users.
     */
    @Test
    @DisplayName("Should find all users")
    void testFindAll_Users() {
        // Given
        entityManager.persistAndFlush(testUser);
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        entityManager.persistAndFlush(anotherUser);

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test updating a user.
     * Verifies that the repository correctly updates user details.
     */
    @Test
    @DisplayName("Should update user successfully")
    void testUpdate_User() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        savedUser.setEmail("newemail@example.com");

        // When
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
    }

    /**
     * Test checking if user exists by ID.
     * Verifies that the repository correctly checks user existence.
     */
    @Test
    @DisplayName("Should check if user exists by ID")
    void testExistsById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test counting users.
     * Verifies that the repository correctly counts users.
     */
    @Test
    @DisplayName("Should count users")
    void testCount_Users() {
        // Given
        entityManager.persistAndFlush(testUser);
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("user2");
        anotherUser.setEmail("user2@example.com");
        entityManager.persistAndFlush(anotherUser);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}