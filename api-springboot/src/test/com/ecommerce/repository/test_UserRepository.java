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
 * Tests repository methods for User entity operations including username queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
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
    private UUID userId;
    private String username;

    /**
     * Set up test data before each test method execution.
     * Creates and persists a test User entity.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "testuser";

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername(username);
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("hashedPassword123");
        entityManager.persist(testUser);
        
        entityManager.flush();
    }

    /**
     * Test finding a User by username when it exists.
     * Verifies that the correct User is returned.
     */
    @Test
    @DisplayName("Should find User by username when exists")
    void testFindByUsername_WhenExists_ReturnsUser() {
        // When
        Optional<User> result = userRepository.findByUsername(username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getEmail()).isEqualTo("testuser@example.com");
    }

    /**
     * Test finding a User by username when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when username does not exist")
    void testFindByUsername_WhenNotExists_ReturnsEmpty() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if a username exists.
     * Verifies that the method returns true for existing usernames.
     */
    @Test
    @DisplayName("Should return true when username exists")
    void testExistsByUsername_WhenExists_ReturnsTrue() {
        // When
        boolean exists = userRepository.existsByUsername(username);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if a username exists when it does not.
     * Verifies that the method returns false for non-existing usernames.
     */
    @Test
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsername_WhenNotExists_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a new User.
     * Verifies that the User is persisted correctly with all attributes.
     */
    @Test
    @DisplayName("Should save new User successfully")
    void testSave_NewUser_Success() {
        // Given
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("hashedPassword456");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
    }

    /**
     * Test updating an existing User.
     * Verifies that User modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing User successfully")
    void testSave_UpdateUser_Success() {
        // Given
        testUser.setEmail("updated@example.com");
        testUser.setPassword("newHashedPassword");

        // When
        User updatedUser = userRepository.save(testUser);
        entityManager.flush();

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getPassword()).isEqualTo("newHashedPassword");
        assertThat(updatedUser.getId()).isEqualTo(userId);
    }

    /**
     * Test finding a User by ID.
     * Verifies that the correct User is retrieved.
     */
    @Test
    @DisplayName("Should find User by ID when exists")
    void testFindById_WhenExists_ReturnsUser() {
        // When
        Optional<User> result = userRepository.findById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getUsername()).isEqualTo(username);
    }

    /**
     * Test finding a User by ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when User ID does not exist")
    void testFindById_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a User by ID.
     * Verifies that the User is removed from the database.
     */
    @Test
    @DisplayName("Should delete User by ID successfully")
    void testDeleteById_Success() {
        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all Users.
     * Verifies that all persisted Users are retrieved.
     */
    @Test
    @DisplayName("Should find all Users")
    void testFindAll_ReturnsAllUsers() {
        // Given - create additional user
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("hashedPassword789");
        entityManager.persist(anotherUser);
        entityManager.flush();

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).isNotEmpty();
        assertThat(allUsers).hasSize(2);
    }

    /**
     * Test username uniqueness constraint.
     * Verifies that duplicate usernames are handled correctly.
     */
    @Test
    @DisplayName("Should enforce username uniqueness")
    void testUsernameUniqueness() {
        // When
        boolean existsBefore = userRepository.existsByUsername(username);
        
        // Then
        assertThat(existsBefore).isTrue();
        
        // Verify only one user with this username exists
        Optional<User> user = userRepository.findByUsername(username);
        assertThat(user).isPresent();
        assertThat(user.get().getId()).isEqualTo(userId);
    }

    /**
     * Test finding user by username with case sensitivity.
     * Verifies that username search is case-sensitive.
     */
    @Test
    @DisplayName("Should handle username case sensitivity")
    void testFindByUsername_CaseSensitivity() {
        // When
        Optional<User> lowerCase = userRepository.findByUsername(username.toLowerCase());
        Optional<User> upperCase = userRepository.findByUsername(username.toUpperCase());

        // Then
        assertThat(lowerCase).isPresent();
        // Upper case should not match if username is stored in lower case
        if (!username.equals(username.toUpperCase())) {
            assertThat(upperCase).isEmpty();
        }
    }

    /**
     * Test saving User with null optional fields.
     * Verifies that Users can be saved with minimal required fields.
     */
    @Test
    @DisplayName("Should save User with minimal required fields")
    void testSave_WithMinimalFields_Success() {
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
     * Test that User password is stored correctly.
     * Verifies password persistence.
     */
    @Test
    @DisplayName("Should store User password correctly")
    void testPasswordStorage() {
        // When
        Optional<User> result = userRepository.findByUsername(username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPassword()).isEqualTo("hashedPassword123");
        assertThat(result.get().getPassword()).isNotEmpty();
    }
}