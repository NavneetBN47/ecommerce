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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
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
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testCart = new Cart();
        testCart.setUserId(testUserId);
    }

    /**
     * Test finding cart by user ID when cart exists.
     * Verifies that the correct cart is returned.
     */
    @Test
    @DisplayName("Should find cart by user ID when exists")
    void testFindByUserId_WhenExists() {
        // Given
        entityManager.persist(testCart);
        entityManager.flush();

        // When
        Optional<Cart> result = cartRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(testUserId);
    }

    /**
     * Test finding cart by user ID when cart does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when cart not found by user ID")
    void testFindByUserId_WhenNotExists() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting cart by user ID when cart exists.
     * Verifies that the cart is removed from the database.
     */
    @Test
    @DisplayName("Should delete cart by user ID successfully")
    void testDeleteByUserId_WhenExists() {
        // Given
        entityManager.persist(testCart);
        entityManager.flush();
        entityManager.clear();

        // When
        cartRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findByUserId(testUserId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting cart by user ID when cart does not exist.
     * Verifies that no exception is thrown.
     */
    @Test
    @DisplayName("Should handle delete by user ID when cart does not exist")
    void testDeleteByUserId_WhenNotExists() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When & Then - should not throw exception
        cartRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();
    }

    /**
     * Test saving a new cart.
     * Verifies that the cart is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new cart successfully")
    void testSave_NewCart() {
        // When
        Cart savedCart = cartRepository.save(testCart);

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getUserId()).isEqualTo(testUserId);
    }

    /**
     * Test updating an existing cart.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing cart successfully")
    void testSave_UpdateCart() {
        // Given
        Cart savedCart = entityManager.persist(testCart);
        entityManager.flush();
        UUID savedId = savedCart.getId();
        UUID newUserId = UUID.randomUUID();

        // When
        savedCart.setUserId(newUserId);
        Cart updatedCart = cartRepository.save(savedCart);

        // Then
        assertThat(updatedCart.getId()).isEqualTo(savedId);
        assertThat(updatedCart.getUserId()).isEqualTo(newUserId);
    }

    /**
     * Test finding cart by ID.
     * Verifies that the correct cart is retrieved.
     */
    @Test
    @DisplayName("Should find cart by ID when exists")
    void testFindById_WhenExists() {
        // Given
        Cart savedCart = entityManager.persist(testCart);
        entityManager.flush();

        // When
        Optional<Cart> result = cartRepository.findById(savedCart.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCart.getId());
    }

    /**
     * Test finding cart by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when cart not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by ID.
     * Verifies that the cart is removed from the database.
     */
    @Test
    @DisplayName("Should delete cart by ID successfully")
    void testDeleteById() {
        // Given
        Cart savedCart = entityManager.persist(testCart);
        entityManager.flush();
        UUID savedId = savedCart.getId();

        // When
        cartRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all carts.
     * Verifies that all persisted carts are retrieved.
     */
    @Test
    @DisplayName("Should find all carts")
    void testFindAll() {
        // Given
        entityManager.persist(testCart);
        
        Cart anotherCart = new Cart();
        anotherCart.setUserId(UUID.randomUUID());
        entityManager.persist(anotherCart);
        entityManager.flush();

        // When
        var allCarts = cartRepository.findAll();

        // Then
        assertThat(allCarts).hasSize(2);
    }

    /**
     * Test checking if cart exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when cart exists by ID")
    void testExistsById_WhenExists() {
        // Given
        Cart savedCart = entityManager.persist(testCart);
        entityManager.flush();

        // When
        boolean exists = cartRepository.existsById(savedCart.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if cart exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when cart does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = cartRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all carts.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all carts correctly")
    void testCount() {
        // Given
        entityManager.persist(testCart);
        Cart anotherCart = new Cart();
        anotherCart.setUserId(UUID.randomUUID());
        entityManager.persist(anotherCart);
        entityManager.flush();

        // When
        long count = cartRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}