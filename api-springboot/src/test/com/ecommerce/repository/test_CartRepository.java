package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for CartRepository.
 * Tests repository operations for Cart entity including custom query methods.
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
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
    }

    /**
     * Test finding a cart by user ID when it exists.
     * Verifies that the custom query method returns the correct cart.
     */
    @Test
    @DisplayName("Should find cart by user ID when exists")
    void testFindByUserId_WhenExists_ShouldReturnCart() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Cart> result = cartRepository.findByUserId(testUserId);

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding a cart by user ID when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when cart does not exist for user")
    void testFindByUserId_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart with null user ID.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null user ID gracefully")
    void testFindByUserId_WithNullUserId_ShouldReturnEmpty() {
        // When
        Optional<Cart> result = cartRepository.findByUserId(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by user ID.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @Transactional
    @DisplayName("Should delete cart by user ID successfully")
    void testDeleteByUserId_ShouldRemoveCart() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();
        UUID savedUserId = testUserId;

        // When
        cartRepository.deleteByUserId(savedUserId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findByUserId(savedUserId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart by non-existent user ID.
     * Verifies that the operation completes without error.
     */
    @Test
    @Transactional
    @DisplayName("Should handle delete by non-existent user ID gracefully")
    void testDeleteByUserId_WithNonExistentUserId_ShouldNotThrowException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            cartRepository.deleteByUserId(nonExistentUserId);
            entityManager.flush();
        });
    }

    /**
     * Test saving a cart.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save cart successfully")
    void testSave_ShouldPersistCart() {
        // When
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
    }

    /**
     * Test finding a cart by ID.
     * Verifies that findById returns the correct cart.
     */
    @Test
    @DisplayName("Should find cart by ID when exists")
    void testFindById_WhenExists_ShouldReturnCart() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();
        UUID savedId = savedCart.getId();

        // When
        Optional<Cart> result = cartRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test finding a cart by ID when it does not exist.
     * Verifies that findById returns empty Optional.
     */
    @Test
    @DisplayName("Should return empty when cart ID does not exist")
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a cart.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete cart successfully")
    void testDelete_ShouldRemoveCart() {
        // Given
        Cart savedCart = cartRepository.save(testCart);
        entityManager.flush();
        UUID savedId = savedCart.getId();

        // When
        cartRepository.delete(savedCart);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(savedId);
        assertThat(result).isEmpty();
    }
}