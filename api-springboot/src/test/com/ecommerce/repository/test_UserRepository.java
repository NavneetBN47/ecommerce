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
 * Tests all public methods including custom query methods.
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
        testUser.setPassword("hashedPassword123");
    }

    /**
     * Test finding user by username - success case.
     */
    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername_Success() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findByUsername(testUsername);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
    }

    /**
     * Test finding user by username - not found case.
     */
    @Test
    @DisplayName("Should return empty when user not found by username")
    void testFindByUsername_NotFound() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if username exists - true case.
     */
    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_True() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByUsername(testUsername);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if username exists - false case.
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
     * Test saving a user.
     */
    @Test
    @DisplayName("Should save user successfully")
    void testSaveUser() {
        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUsername);
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
    }

    /**
     * Test finding user by ID.
     */
    @Test
    @DisplayName("Should find user by ID")
    void testFindById_Success() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findById(savedUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    /**
     * Test finding user by ID - not found.
     */
    @Test
    @DisplayName("Should return empty when user not found by ID")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a user.
     */
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
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
     */
    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Given
        userRepository.save(testUser);
        
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password123");
        userRepository.save(anotherUser);
        entityManager.flush();

        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test updating a user.
     */
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // When
        savedUser.setEmail("newemail@example.com");
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
    }

    /**
     * Test with null username - edge case.
     */
    @Test
    @DisplayName("Should handle null username gracefully")
    void testFindByUsername_NullUsername() {
        // When
        Optional<User> result = userRepository.findByUsername(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test exists by username with null - edge case.
     */
    @Test
    @DisplayName("Should handle null username in exists check")
    void testExistsByUsername_NullUsername() {
        // When
        boolean exists = userRepository.existsByUsername(null);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test case sensitivity of username search.
     */
    @Test
    @DisplayName("Should handle username case sensitivity")
    void testFindByUsername_CaseSensitive() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        Optional<User> resultLower = userRepository.findByUsername(testUsername.toLowerCase());
        Optional<User> resultUpper = userRepository.findByUsername(testUsername.toUpperCase());

        // Then
        assertThat(resultLower).isPresent();
        // Upper case should not match if username is case-sensitive
        assertThat(resultUpper).isEmpty();
    }

    /**
     * Test counting users.
     */
    @Test
    @DisplayName("Should count users correctly")
    void testCount() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Test checking if user exists by ID.
     */
    @Test
    @DisplayName("Should return true when user exists by ID")
    void testExistsById_True() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if user exists by ID - false case.
     */
    @Test
    @DisplayName("Should return false when user does not exist by ID")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = userRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}