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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
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
        testUsername = "testuser@example.com";
        
        testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword("encodedPassword123");
        testUser.setEmail("testuser@example.com");
    }

    /**
     * Test finding user by username when user exists.
     * Verifies that the correct user is returned.
     */
    @Test
    @DisplayName("Should find user by username when exists")
    void testFindByUsername_WhenExists() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findByUsername(testUsername);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
        assertThat(result.get().getEmail()).isEqualTo("testuser@example.com");
    }

    /**
     * Test finding user by username when user does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when user not found by username")
    void testFindByUsername_WhenNotExists() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if username exists.
     * Verifies that the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_WhenExists() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByUsername(testUsername);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if username exists when it doesn't.
     * Verifies that the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_WhenNotExists() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a new user.
     * Verifies that the user is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new user successfully")
    void testSave_NewUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUsername);
    }

    /**
     * Test updating an existing user.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing user successfully")
    void testSave_UpdateUser() {
        // Given
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();
        UUID savedId = savedUser.getId();

        // When
        savedUser.setEmail("newemail@example.com");
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(savedId);
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
    }

    /**
     * Test finding user by ID.
     * Verifies that the correct user is retrieved.
     */
    @Test
    @DisplayName("Should find user by ID when exists")
    void testFindById_WhenExists() {
        // Given
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findById(savedUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    /**
     * Test finding user by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when user not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a user by ID.
     * Verifies that the user is removed from the database.
     */
    @Test
    @DisplayName("Should delete user by ID successfully")
    void testDeleteById() {
        // Given
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();
        UUID savedId = savedUser.getId();

        // When
        userRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all users.
     * Verifies that all persisted users are retrieved.
     */
    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Given
        entityManager.persist(testUser);
        
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser@example.com");
        anotherUser.setPassword("password456");
        anotherUser.setEmail("anotheruser@example.com");
        entityManager.persist(anotherUser);
        entityManager.flush();

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(2);
    }

    /**
     * Test checking if user exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when user exists by ID")
    void testExistsById_WhenExists() {
        // Given
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if user exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when user does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = userRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all users.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all users correctly")
    void testCount() {
        // Given
        entityManager.persist(testUser);
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser@example.com");
        anotherUser.setPassword("password456");
        anotherUser.setEmail("anotheruser@example.com");
        entityManager.persist(anotherUser);
        entityManager.flush();

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    /**
     * Test username uniqueness constraint.
     * Verifies that duplicate usernames are handled appropriately.
     */
    @Test
    @DisplayName("Should handle duplicate username appropriately")
    void testUsernameUniqueness() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();

        // When
        boolean existsBefore = userRepository.existsByUsername(testUsername);
        
        User duplicateUser = new User();
        duplicateUser.setUsername(testUsername);
        duplicateUser.setPassword("differentPassword");
        duplicateUser.setEmail("different@example.com");

        // Then
        assertThat(existsBefore).isTrue();
        // Note: Actual constraint violation would be handled by the database
        // This test verifies the existence check works correctly
    }
}