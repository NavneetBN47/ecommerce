package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for CartRepository.
 * Tests repository methods for Cart entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("CartRepository Tests")
public class test_CartRepository {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testUserId;
    private Cart testCart;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
    }

    /**
     * Test finding a cart by user ID when it exists.
     * Verifies that the repository correctly retrieves a cart for a given user.
     */
    @Test
    @DisplayName("Should find cart by user ID when exists")
    void testFindByUserId_WhenExists() {
        // Given
        Cart savedCart = entityManager.persistAndFlush(testCart);

        // When
        Optional<Cart> result = cartRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding a cart by user ID when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent cart.
     */
    @Test
    @DisplayName("Should return empty when cart does not exist for user")
    void testFindByUserId_WhenNotExists() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by user ID.
     * Verifies that the repository correctly deletes a cart for a given user.
     */
    @Test
    @Transactional
    @DisplayName("Should delete cart by user ID")
    void testDeleteByUserId() {
        // Given
        Cart savedCart = entityManager.persistAndFlush(testCart);
        UUID cartId = savedCart.getId();

        // When
        cartRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a cart.
     * Verifies that the repository correctly persists a cart.
     */
    @Test
    @DisplayName("Should save cart successfully")
    void testSave_Cart() {
        // When
        Cart savedCart = cartRepository.save(testCart);

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
    }

    /**
     * Test finding a cart by ID.
     * Verifies that the repository correctly retrieves a cart by its ID.
     */
    @Test
    @DisplayName("Should find cart by ID")
    void testFindById_WhenExists() {
        // Given
        Cart savedCart = entityManager.persistAndFlush(testCart);

        // When
        Optional<Cart> result = cartRepository.findById(savedCart.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCart.getId());
    }

    /**
     * Test deleting a cart entity.
     * Verifies that the repository correctly deletes a cart.
     */
    @Test
    @DisplayName("Should delete cart entity successfully")
    void testDelete_Cart() {
        // Given
        Cart savedCart = entityManager.persistAndFlush(testCart);
        UUID cartId = savedCart.getId();

        // When
        cartRepository.delete(savedCart);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all carts.
     * Verifies that the repository correctly retrieves all carts.
     */
    @Test
    @DisplayName("Should find all carts")
    void testFindAll_Carts() {
        // Given
        entityManager.persistAndFlush(testCart);
        Cart anotherCart = new Cart();
        anotherCart.setId(UUID.randomUUID());
        entityManager.persistAndFlush(anotherCart);

        // When
        var allCarts = cartRepository.findAll();

        // Then
        assertThat(allCarts).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test checking if cart exists by ID.
     * Verifies that the repository correctly checks cart existence.
     */
    @Test
    @DisplayName("Should check if cart exists by ID")
    void testExistsById() {
        // Given
        Cart savedCart = entityManager.persistAndFlush(testCart);

        // When
        boolean exists = cartRepository.existsById(savedCart.getId());

        // Then
        assertThat(exists).isTrue();
    }
}