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
 * Tests repository methods for User entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class test_UserRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    /**
     * Set up test data before each test method execution.
     * Creates and persists a test user in the in-memory database.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("testuser@example.com");
        entityManager.persist(testUser);
        entityManager.flush();
    }

    /**
     * Test finding a User by username.
     * Verifies that the custom query method returns the correct User.
     */
    @Test
    @DisplayName("Should find User by username")
    void testFindByUsername_Success() {
        // When
        Optional<User> result = userRepository.findByUsername("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("testuser@example.com");
    }

    /**
     * Test finding a User with non-existent username.
     * Verifies that the method returns empty Optional when username doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when username does not exist")
    void testFindByUsername_NotFound() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if username exists.
     * Verifies that the method returns true for existing username.
     */
    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_True() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if username exists with non-existent username.
     * Verifies that the method returns false for non-existent username.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_False() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a new User.
     * Verifies that the repository can persist a new User entity.
     */
    @Test
    @DisplayName("Should save a new User successfully")
    void testSave_NewUser() {
        // Given
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        newUser.setEmail("newuser@example.com");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
    }

    /**
     * Test updating an existing User.
     * Verifies that the repository can update User properties.
     */
    @Test
    @DisplayName("Should update existing User successfully")
    void testSave_UpdateUser() {
        // Given
        testUser.setEmail("updated@example.com");

        // When
        User updatedUser = userRepository.save(testUser);

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    /**
     * Test finding a User by ID.
     * Verifies that the repository can retrieve a User by its primary key.
     */
    @Test
    @DisplayName("Should find User by ID")
    void testFindById_Success() {
        // When
        Optional<User> result = userRepository.findById(testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding a User with non-existent ID.
     * Verifies that the method returns empty Optional for non-existent user.
     */
    @Test
    @DisplayName("Should return empty Optional when User ID does not exist")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a User.
     * Verifies that the repository can delete a User entity.
     */
    @Test
    @DisplayName("Should delete User successfully")
    void testDelete_Success() {
        // Given
        UUID userId = testUser.getId();

        // When
        userRepository.delete(testUser);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all Users.
     * Verifies that the repository can count the total number of Users.
     */
    @Test
    @DisplayName("Should count all Users")
    void testCount_Success() {
        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if User exists by ID.
     * Verifies that the repository can check existence of a User.
     */
    @Test
    @DisplayName("Should return true when User exists by ID")
    void testExistsById_True() {
        // When
        boolean exists = userRepository.existsById(testUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if User exists with non-existent ID.
     * Verifies that the repository returns false for non-existent User.
     */
    @Test
    @DisplayName("Should return false when User does not exist by ID")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = userRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding User by username is case-sensitive.
     * Verifies that username search is case-sensitive.
     */
    @Test
    @DisplayName("Should be case-sensitive when finding by username")
    void testFindByUsername_CaseSensitive() {
        // When
        Optional<User> result = userRepository.findByUsername("TESTUSER");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking username existence is case-sensitive.
     * Verifies that username existence check is case-sensitive.
     */
    @Test
    @DisplayName("Should be case-sensitive when checking username existence")
    void testExistsByUsername_CaseSensitive() {
        // When
        boolean exists = userRepository.existsByUsername("TESTUSER");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving User with minimum required fields.
     * Verifies that User can be saved with only required fields.
     */
    @Test
    @DisplayName("Should save User with minimum required fields")
    void testSave_MinimumFields() {
        // Given
        User minimalUser = new User();
        minimalUser.setId(UUID.randomUUID());
        minimalUser.setUsername("minimaluser");
        minimalUser.setPassword("password");

        // When
        User savedUser = userRepository.save(minimalUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("minimaluser");
    }

    /**
     * Test finding all Users.
     * Verifies that the repository can retrieve all Users.
     */
    @Test
    @DisplayName("Should find all Users")
    void testFindAll_Success() {
        // When
        var result = userRepository.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    /**
     * Test deleting User by ID.
     * Verifies that the repository can delete a User by its primary key.
     */
    @Test
    @DisplayName("Should delete User by ID successfully")
    void testDeleteById_Success() {
        // Given
        UUID userId = testUser.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test saving multiple Users with different usernames.
     * Verifies that multiple users can be saved with unique usernames.
     */
    @Test
    @DisplayName("Should save multiple Users with different usernames")
    void testSave_MultipleUsers() {
        // Given
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        user2.setPassword("password2");

        User user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setUsername("user3");
        user3.setPassword("password3");

        // When
        userRepository.save(user2);
        userRepository.save(user3);
        entityManager.flush();

        // Then
        assertThat(userRepository.existsByUsername("user2")).isTrue();
        assertThat(userRepository.existsByUsername("user3")).isTrue();
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(3);
    }

    /**
     * Test updating User password.
     * Verifies that User password can be updated.
     */
    @Test
    @DisplayName("Should update User password successfully")
    void testSave_UpdatePassword() {
        // Given
        String newPassword = "newSecurePassword123";
        testUser.setPassword(newPassword);

        // When
        User updatedUser = userRepository.save(testUser);

        // Then
        assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
    }
}