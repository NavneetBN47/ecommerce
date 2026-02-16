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
 * Tests repository methods for User entity operations including
 * finding by username and checking username existence.
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

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
    }

    /**
     * Test finding a user by username when it exists.
     * Verifies that the correct user is returned.
     */
    @Test
    @DisplayName("Should find user by username when exists")
    void testFindByUsername_WhenExists_ReturnsUser() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john.doe");
        entityManager.persistAndFlush(user);

        // When
        Optional<User> result = userRepository.findByUsername("john.doe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john.doe");
    }

    /**
     * Test finding a user by username when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when username does not exist")
    void testFindByUsername_WhenNotExists_ReturnsEmpty() {
        // Given
        String nonExistentUsername = "nonexistent";

        // When
        Optional<User> result = userRepository.findByUsername(nonExistentUsername);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if username exists.
     * Verifies that the existence check returns true for existing usernames.
     */
    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername_WhenExists_ReturnsTrue() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("existing.user");
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByUsername("existing.user");

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if username exists when it doesn't.
     * Verifies that the existence check returns false for non-existent usernames.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_WhenNotExists_ReturnsFalse() {
        // Given
        String nonExistentUsername = "nonexistent.user";

        // When
        boolean exists = userRepository.existsByUsername(nonExistentUsername);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a user.
     * Verifies that the user is persisted correctly.
     */
    @Test
    @DisplayName("Should save user successfully")
    void testSave_ValidUser_SavesSuccessfully() {
        // Given
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername("new.user");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("new.user");
    }

    /**
     * Test finding a user by ID.
     * Verifies that the user can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find user by ID when exists")
    void testFindById_WhenExists_ReturnsUser() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("find.user");
        User savedUser = entityManager.persistAndFlush(user);

        // When
        Optional<User> result = userRepository.findById(savedUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    /**
     * Test deleting a user.
     * Verifies that the user is removed from the database.
     */
    @Test
    @DisplayName("Should delete user successfully")
    void testDelete_ExistingUser_DeletesSuccessfully() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("delete.user");
        User savedUser = entityManager.persistAndFlush(user);
        UUID userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all users.
     * Verifies that all users can be retrieved.
     */
    @Test
    @DisplayName("Should find all users")
    void testFindAll_ReturnsAllUsers() {
        // Given
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername("user1");
        
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        var result = userRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting users.
     * Verifies that the count of users is correct.
     */
    @Test
    @DisplayName("Should count users correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = userRepository.count();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("count.user");
        entityManager.persistAndFlush(user);

        // When
        long newCount = userRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test checking if user exists by ID.
     * Verifies that existence check works correctly.
     */
    @Test
    @DisplayName("Should check if user exists by ID")
    void testExistsById_WhenExists_ReturnsTrue() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("exists.user");
        User savedUser = entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test updating a user.
     * Verifies that the user can be updated successfully.
     */
    @Test
    @DisplayName("Should update user successfully")
    void testUpdate_ExistingUser_UpdatesSuccessfully() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("original.username");
        User savedUser = entityManager.persistAndFlush(user);
        
        savedUser.setUsername("updated.username");

        // When
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("updated.username");
    }
}