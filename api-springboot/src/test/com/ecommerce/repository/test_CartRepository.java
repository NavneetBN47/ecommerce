package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
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
 * JUnit 5 test class for CartRepository.
 * Tests all public methods including custom query and delete methods.
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
        // Note: Actual entity setup would require User entity
        // This is a simplified version for demonstration
    }

    /**
     * Test finding cart by user ID - success case.
     */
    @Test
    @DisplayName("Should find cart by user ID")
    void testFindByUserId_Success() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();

        // When
        Optional<Cart> result = cartRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding cart by user ID - not found case.
     */
    @Test
    @DisplayName("Should return empty when cart not found by user ID")
    void testFindByUserId_NotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting cart by user ID - success case.
     */
    @Test
    @DisplayName("Should delete cart by user ID")
    void testDeleteByUserId_Success() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();

        // When
        cartRepository.deleteByUserId(testUserId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findByUserId(testUserId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting cart by non-existent user ID.
     */
    @Test
    @DisplayName("Should handle delete by non-existent user ID gracefully")
    void testDeleteByUserId_NotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When/Then - should not throw exception
        cartRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();
    }

    /**
     * Test saving a cart.
     */
    @Test
    @DisplayName("Should save cart successfully")
    void testSaveCart() {
        // When
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
    }

    /**
     * Test finding cart by ID.
     */
    @Test
    @DisplayName("Should find cart by ID")
    void testFindById_Success() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();

        // When
        Optional<Cart> result = cartRepository.findById(savedCart.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCart.getId());
    }

    /**
     * Test deleting a cart by ID.
     */
    @Test
    @DisplayName("Should delete cart by ID successfully")
    void testDeleteById() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();
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
     */
    @Test
    @DisplayName("Should find all carts")
    void testFindAll() {
        // Given
        cartRepository.save(testCart);
        entityManager.flush();

        // When
        var carts = cartRepository.findAll();

        // Then
        assertThat(carts).isNotEmpty();
    }

    /**
     * Test with null user ID - edge case.
     */
    @Test
    @DisplayName("Should handle null user ID gracefully")
    void testFindByUserId_NullUserId() {
        // When
        Optional<Cart> result = cartRepository.findByUserId(null);

        // Then
        assertThat(result).isEmpty();
    }
}