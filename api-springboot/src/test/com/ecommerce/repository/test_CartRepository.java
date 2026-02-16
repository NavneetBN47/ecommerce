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
 * Tests all repository methods including custom query methods for cart operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartRepository Tests")
public class test_CartRepository {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Cart testCart;
    private UUID userId;

    /**
     * Set up test data before each test method execution.
     * Creates test user and cart entities.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
        testCart.setUser(testUser);
        entityManager.persist(testCart);

        entityManager.flush();
    }

    /**
     * Test finding a cart by user ID.
     * Verifies that the correct cart is retrieved for a given user.
     */
    @Test
    @DisplayName("Should find cart by user ID")
    void testFindByUserId_Success() {
        Optional<Cart> result = cartRepository.findByUserId(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(userId);
        assertThat(result.get().getId()).isEqualTo(testCart.getId());
    }

    /**
     * Test finding a cart with non-existent user ID.
     * Verifies that an empty Optional is returned when user ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when user ID not found")
    void testFindByUserId_NotFound() {
        UUID nonExistentUserId = UUID.randomUUID();

        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart by user ID with null parameter.
     * Verifies proper handling of null user ID.
     */
    @Test
    @DisplayName("Should handle null user ID")
    void testFindByUserId_NullUserId() {
        Optional<Cart> result = cartRepository.findByUserId(null);

        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by user ID.
     * Verifies that a cart is successfully deleted for a given user.
     */
    @Test
    @DisplayName("Should delete cart by user ID")
    void testDeleteByUserId_Success() {
        cartRepository.deleteByUserId(userId);
        entityManager.flush();

        Optional<Cart> result = cartRepository.findByUserId(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart with non-existent user ID.
     * Verifies that delete operation handles non-existent user gracefully.
     */
    @Test
    @DisplayName("Should handle delete with non-existent user ID")
    void testDeleteByUserId_NotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        long countBefore = cartRepository.count();

        cartRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();

        long countAfter = cartRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test saving a new cart.
     * Verifies that a cart can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new cart successfully")
    void testSaveCart() {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        entityManager.persist(newUser);

        Cart newCart = new Cart();
        newCart.setId(UUID.randomUUID());
        newCart.setUser(newUser);

        Cart saved = cartRepository.save(newCart);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(newUser.getId());
    }

    /**
     * Test finding a cart by ID.
     * Verifies that a cart can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find cart by ID")
    void testFindById_Success() {
        Optional<Cart> result = cartRepository.findById(testCart.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testCart.getId());
    }

    /**
     * Test finding a cart with non-existent ID.
     * Verifies that an empty Optional is returned when cart ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when cart ID not found")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<Cart> result = cartRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by ID.
     * Verifies that a cart can be successfully deleted by its ID.
     */
    @Test
    @DisplayName("Should delete cart by ID")
    void testDeleteById() {
        UUID cartId = testCart.getId();
        cartRepository.deleteById(cartId);
        entityManager.flush();

        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all carts.
     * Verifies that all carts can be retrieved.
     */
    @Test
    @DisplayName("Should find all carts")
    void testFindAll() {
        assertThat(cartRepository.findAll()).isNotEmpty();
        assertThat(cartRepository.findAll()).hasSize(1);
    }
}