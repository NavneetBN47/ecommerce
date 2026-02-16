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
 * JUnit test class for UserRepository.
 * Tests all repository methods including custom query methods for user operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
public class test_UserRepository {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private String testUsername;

    /**
     * Set up test data before each test method execution.
     * Creates test user entity.
     */
    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(testUsername);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        entityManager.persist(testUser);
        entityManager.flush();
    }

    /**
     * Test finding a user by username.
     * Verifies that the correct user is retrieved by username.
     */
    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername_Success() {
        Optional<User> result = userRepository.findByUsername(testUsername);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    /**
     * Test finding a user with non-existent username.
     * Verifies that an empty Optional is returned when username doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByUsername_NotFound() {
        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    /**
     * Test finding a user with null username.
     * Verifies proper handling of null username.
     */
    @Test
    @DisplayName("Should handle null username")
    void testFindByUsername_NullUsername() {
        Optional<User> result = userRepository.findByUsername(null);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding a user by username case-sensitively.
     * Verifies that username search is case-sensitive.
     */
    @Test
    @DisplayName("Should find user by username case-sensitively")
    void testFindByUsername_CaseSensitive() {
        Optional<User> resultLower = userRepository.findByUsername("testuser");
        Optional<User> resultUpper = userRepository.findByUsername("TESTUSER");

        assertThat(resultLower).isPresent();
        assertThat(resultUpper).isEmpty();
    }

    /**
     * Test checking if username exists.
     * Verifies that existence check returns true for existing username.
     */
    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_Exists() {
        boolean exists = userRepository.existsByUsername(testUsername);

        assertThat(exists).isTrue();
    }

    /**
     * Test checking if non-existent username exists.
     * Verifies that existence check returns false for non-existent username.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_NotExists() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    /**
     * Test checking username existence with null.
     * Verifies proper handling of null username in existence check.
     */
    @Test
    @DisplayName("Should handle null username in existence check")
    void testExistsByUsername_NullUsername() {
        boolean exists = userRepository.existsByUsername(null);

        assertThat(exists).isFalse();
    }

    /**
     * Test checking username existence case-sensitively.
     * Verifies that existence check is case-sensitive.
     */
    @Test
    @DisplayName("Should check username existence case-sensitively")
    void testExistsByUsername_CaseSensitive() {
        boolean existsLower = userRepository.existsByUsername("testuser");
        boolean existsUpper = userRepository.existsByUsername("TESTUSER");

        assertThat(existsLower).isTrue();
        assertThat(existsUpper).isFalse();
    }

    /**
     * Test saving a new user.
     * Verifies that a user can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new user successfully")
    void testSaveUser() {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("hashedPassword456");

        User saved = userRepository.save(newUser);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
    }

    /**
     * Test updating an existing user.
     * Verifies that user details can be updated.
     */
    @Test
    @DisplayName("Should update existing user")
    void testUpdateUser() {
        testUser.setEmail("updated@example.com");
        User updated = userRepository.save(testUser);

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding a user by ID.
     * Verifies that a user can be retrieved by their ID.
     */
    @Test
    @DisplayName("Should find user by ID")
    void testFindById_Success() {
        Optional<User> result = userRepository.findById(testUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testUser.getId());
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
    }

    /**
     * Test finding a user with non-existent ID.
     * Verifies that an empty Optional is returned when ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when user ID not found")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<User> result = userRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a user by ID.
     * Verifies that a user can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteUser() {
        UUID userId = testUser.getId();
        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all users.
     * Verifies that all users can be retrieved.
     */
    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        assertThat(userRepository.findAll()).isNotEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
    }

    /**
     * Test counting users.
     * Verifies that the count of users is accurate.
     */
    @Test
    @DisplayName("Should count users correctly")
    void testCount() {
        long count = userRepository.count();

        assertThat(count).isEqualTo(1L);
    }

    /**
     * Test saving multiple users with unique usernames.
     * Verifies that multiple users can be persisted.
     */
    @Test
    @DisplayName("Should save multiple users with unique usernames")
    void testSaveMultipleUsers() {
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        userRepository.save(user2);

        User user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setUsername("user3");
        user3.setEmail("user3@example.com");
        user3.setPassword("password3");
        userRepository.save(user3);

        assertThat(userRepository.findAll()).hasSize(3);
    }

    /**
     * Test finding user with empty username.
     * Verifies proper handling of empty username.
     */
    @Test
    @DisplayName("Should handle empty username")
    void testFindByUsername_EmptyUsername() {
        Optional<User> result = userRepository.findByUsername("");

        assertThat(result).isEmpty();
    }
}