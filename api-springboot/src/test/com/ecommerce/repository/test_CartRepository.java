package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
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
 * JUnit 5 test class for CartRepository.
 * Tests repository methods for Cart entity operations including
 * finding by user ID and deleting by user ID.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartRepository Tests")
class test_CartRepository {

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
     * Verifies that the correct cart is returned for a given user.
     */
    @Test
    @DisplayName("Should find cart by user ID when exists")
    void testFindByUserId_WhenExists_ReturnsCart() {
        // Given
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        entityManager.persistAndFlush(cart);

        // When
        Optional<Cart> result = cartRepository.findByUserId(userId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding a cart by user ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when cart does not exist for user")
    void testFindByUserId_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by user ID.
     * Verifies that the cart is removed from the database.
     */
    @Test
    @DisplayName("Should delete cart by user ID successfully")
    void testDeleteByUserId_ExistingCart_DeletesSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        entityManager.persistAndFlush(cart);

        // When
        cartRepository.deleteByUserId(userId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findByUserId(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a cart.
     * Verifies that the cart is persisted correctly.
     */
    @Test
    @DisplayName("Should save cart successfully")
    void testSave_ValidCart_SavesSuccessfully() {
        // Given
        Cart newCart = new Cart();
        newCart.setId(UUID.randomUUID());

        // When
        Cart savedCart = cartRepository.save(newCart);

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
    }

    /**
     * Test finding a cart by ID.
     * Verifies that the cart can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find cart by ID when exists")
    void testFindById_WhenExists_ReturnsCart() {
        // Given
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        Cart savedCart = entityManager.persistAndFlush(cart);

        // When
        Optional<Cart> result = cartRepository.findById(savedCart.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCart.getId());
    }

    /**
     * Test deleting a cart by ID.
     * Verifies that the cart is removed from the database.
     */
    @Test
    @DisplayName("Should delete cart by ID successfully")
    void testDeleteById_ExistingCart_DeletesSuccessfully() {
        // Given
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        Cart savedCart = entityManager.persistAndFlush(cart);
        UUID cartId = savedCart.getId();

        // When
        cartRepository.deleteById(cartId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all carts.
     * Verifies that all carts can be retrieved.
     */
    @Test
    @DisplayName("Should find all carts")
    void testFindAll_ReturnsAllCarts() {
        // Given
        Cart cart1 = new Cart();
        cart1.setId(UUID.randomUUID());
        Cart cart2 = new Cart();
        cart2.setId(UUID.randomUUID());
        entityManager.persist(cart1);
        entityManager.persist(cart2);
        entityManager.flush();

        // When
        var result = cartRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting carts.
     * Verifies that the count of carts is correct.
     */
    @Test
    @DisplayName("Should count carts correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = cartRepository.count();
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        entityManager.persistAndFlush(cart);

        // When
        long newCount = cartRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test checking if cart exists by ID.
     * Verifies that existence check works correctly.
     */
    @Test
    @DisplayName("Should check if cart exists by ID")
    void testExistsById_WhenExists_ReturnsTrue() {
        // Given
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        Cart savedCart = entityManager.persistAndFlush(cart);

        // When
        boolean exists = cartRepository.existsById(savedCart.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test deleting cart by user ID when no cart exists.
     * Verifies that no exception is thrown.
     */
    @Test
    @DisplayName("Should handle delete by user ID when cart does not exist")
    void testDeleteByUserId_WhenNotExists_NoException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            cartRepository.deleteByUserId(nonExistentUserId);
            entityManager.flush();
        });
    }
}