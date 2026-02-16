package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
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
 * JUnit test class for CartRepository.
 * Tests repository methods for Cart entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Cart Repository Tests")
class test_CartRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    private User testUser;
    private Cart testCart;
    private UUID userId;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities in the in-memory database.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        entityManager.persist(testUser);

        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
        testCart.setUserId(userId);
        entityManager.persist(testCart);

        entityManager.flush();
    }

    /**
     * Test finding a Cart by user ID.
     * Verifies that the custom query method returns the correct Cart for a given user.
     */
    @Test
    @DisplayName("Should find Cart by user ID")
    void testFindByUserId_Success() {
        // When
        Optional<Cart> result = cartRepository.findByUserId(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getId()).isEqualTo(testCart.getId());
    }

    /**
     * Test finding a Cart with non-existent user ID.
     * Verifies that the method returns empty Optional when user doesn't have a cart.
     */
    @Test
    @DisplayName("Should return empty Optional when user ID does not exist")
    void testFindByUserId_NotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Cart by user ID.
     * Verifies that the custom delete method removes the cart for a specific user.
     */
    @Test
    @DisplayName("Should delete Cart by user ID")
    void testDeleteByUserId_Success() {
        // When
        cartRepository.deleteByUserId(userId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findByUserId(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Cart with non-existent user ID.
     * Verifies that the delete operation handles non-existent user gracefully.
     */
    @Test
    @DisplayName("Should handle delete by non-existent user ID gracefully")
    void testDeleteByUserId_NotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        long initialCount = cartRepository.count();

        // When
        cartRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();

        // Then
        long finalCount = cartRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Test saving a new Cart.
     * Verifies that the repository can persist a new Cart entity.
     */
    @Test
    @DisplayName("Should save a new Cart successfully")
    void testSave_NewCart() {
        // Given
        UUID newUserId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(newUserId);
        newUser.setUsername("newuser");
        newUser.setPassword("password456");
        entityManager.persist(newUser);

        Cart newCart = new Cart();
        newCart.setId(UUID.randomUUID());
        newCart.setUserId(newUserId);

        // When
        Cart savedCart = cartRepository.save(newCart);

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getUserId()).isEqualTo(newUserId);
    }

    /**
     * Test updating an existing Cart.
     * Verifies that the repository can update Cart properties.
     */
    @Test
    @DisplayName("Should update existing Cart successfully")
    void testSave_UpdateCart() {
        // Given
        UUID newUserId = UUID.randomUUID();
        testCart.setUserId(newUserId);

        // When
        Cart updatedCart = cartRepository.save(testCart);

        // Then
        assertThat(updatedCart.getUserId()).isEqualTo(newUserId);
    }

    /**
     * Test finding a Cart by ID.
     * Verifies that the repository can retrieve a Cart by its primary key.
     */
    @Test
    @DisplayName("Should find Cart by ID")
    void testFindById_Success() {
        // When
        Optional<Cart> result = cartRepository.findById(testCart.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testCart.getId());
    }

    /**
     * Test finding a Cart with non-existent ID.
     * Verifies that the method returns empty Optional for non-existent cart.
     */
    @Test
    @DisplayName("Should return empty Optional when Cart ID does not exist")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Cart by entity.
     * Verifies that the repository can delete a Cart entity.
     */
    @Test
    @DisplayName("Should delete Cart successfully")
    void testDelete_Success() {
        // Given
        UUID cartId = testCart.getId();

        // When
        cartRepository.delete(testCart);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all Carts.
     * Verifies that the repository can count the total number of Carts.
     */
    @Test
    @DisplayName("Should count all Carts")
    void testCount_Success() {
        // When
        long count = cartRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if Cart exists by ID.
     * Verifies that the repository can check existence of a Cart.
     */
    @Test
    @DisplayName("Should return true when Cart exists")
    void testExistsById_True() {
        // When
        boolean exists = cartRepository.existsById(testCart.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if Cart exists with non-existent ID.
     * Verifies that the repository returns false for non-existent Cart.
     */
    @Test
    @DisplayName("Should return false when Cart does not exist")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = cartRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding multiple carts for different users.
     * Verifies that each user has their own separate cart.
     */
    @Test
    @DisplayName("Should find separate carts for different users")
    void testFindByUserId_MultipleCarts() {
        // Given
        UUID userId2 = UUID.randomUUID();
        User user2 = new User();
        user2.setId(userId2);
        user2.setUsername("testuser2");
        user2.setPassword("password789");
        entityManager.persist(user2);

        Cart cart2 = new Cart();
        cart2.setId(UUID.randomUUID());
        cart2.setUserId(userId2);
        entityManager.persist(cart2);
        entityManager.flush();

        // When
        Optional<Cart> result1 = cartRepository.findByUserId(userId);
        Optional<Cart> result2 = cartRepository.findByUserId(userId2);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().getId()).isNotEqualTo(result2.get().getId());
        assertThat(result1.get().getUserId()).isEqualTo(userId);
        assertThat(result2.get().getUserId()).isEqualTo(userId2);
    }
}