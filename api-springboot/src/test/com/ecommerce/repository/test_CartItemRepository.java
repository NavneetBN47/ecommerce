package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
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
 * JUnit 5 test class for CartItemRepository.
 * Tests repository operations for CartItem entity including custom query methods.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartItemRepository Tests")
class test_CartItemRepository {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testCartId;
    private UUID testProductId;
    private CartItem testCartItem;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testCartId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        
        testCartItem = new CartItem();
        testCartItem.setId(UUID.randomUUID());
        // Note: In real scenario, you would set cart and product entities
        // For this test, we're focusing on repository method testing
    }

    /**
     * Test finding a cart item by cart ID and product ID when it exists.
     * Verifies that the custom query method returns the correct cart item.
     */
    @Test
    @DisplayName("Should find cart item by cart ID and product ID when exists")
    void testFindByCartIdAndProductId_WhenExists_ShouldReturnCartItem() {
        // Given
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(testCartId, testProductId);

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding a cart item by cart ID and product ID when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when cart item does not exist")
    void testFindByCartIdAndProductId_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentCartId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart item with null cart ID.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null cart ID gracefully")
    void testFindByCartIdAndProductId_WithNullCartId_ShouldReturnEmpty() {
        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(null, testProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart item with null product ID.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null product ID gracefully")
    void testFindByCartIdAndProductId_WithNullProductId_ShouldReturnEmpty() {
        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(testCartId, null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a cart item.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save cart item successfully")
    void testSave_ShouldPersistCartItem() {
        // When
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();

        // Then
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
    }

    /**
     * Test finding a cart item by ID.
     * Verifies that findById returns the correct cart item.
     */
    @Test
    @DisplayName("Should find cart item by ID when exists")
    void testFindById_WhenExists_ShouldReturnCartItem() {
        // Given
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();
        UUID savedId = savedItem.getId();

        // When
        Optional<CartItem> result = cartItemRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test deleting a cart item.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete cart item successfully")
    void testDelete_ShouldRemoveCartItem() {
        // Given
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();
        UUID savedId = savedItem.getId();

        // When
        cartItemRepository.delete(savedItem);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all cart items.
     * Verifies that the count operation returns correct number.
     */
    @Test
    @DisplayName("Should count all cart items correctly")
    void testCount_ShouldReturnCorrectCount() {
        // Given
        cartItemRepository.save(testCartItem);
        entityManager.flush();

        // When
        long count = cartItemRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}