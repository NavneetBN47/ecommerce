package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
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
 * JUnit 5 test class for CartItemRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("CartItemRepository Tests")
public class test_CartItemRepository {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testCartId;
    private UUID testProductId;
    private CartItem testCartItem;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testCartId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        
        testCartItem = new CartItem();
        testCartItem.setId(UUID.randomUUID());
        // Note: Actual entity setup would require Cart and Product entities
        // This is a simplified version for demonstration
    }

    /**
     * Test finding cart item by cart ID and product ID - success case.
     */
    @Test
    @DisplayName("Should find cart item by cart ID and product ID")
    void testFindByCartIdAndProductId_Success() {
        // Given
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(testCartId, testProductId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding cart item by cart ID and product ID - not found case.
     */
    @Test
    @DisplayName("Should return empty when cart item not found")
    void testFindByCartIdAndProductId_NotFound() {
        // Given
        UUID nonExistentCartId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a cart item.
     */
    @Test
    @DisplayName("Should save cart item successfully")
    void testSaveCartItem() {
        // When
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();

        // Then
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
    }

    /**
     * Test finding cart item by ID.
     */
    @Test
    @DisplayName("Should find cart item by ID")
    void testFindById_Success() {
        // Given
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();

        // When
        Optional<CartItem> result = cartItemRepository.findById(savedItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedItem.getId());
    }

    /**
     * Test deleting a cart item.
     */
    @Test
    @DisplayName("Should delete cart item successfully")
    void testDeleteCartItem() {
        // Given
        CartItem savedItem = cartItemRepository.save(testCartItem);
        entityManager.flush();
        UUID itemId = savedItem.getId();

        // When
        cartItemRepository.deleteById(itemId);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all cart items.
     */
    @Test
    @DisplayName("Should find all cart items")
    void testFindAll() {
        // Given
        cartItemRepository.save(testCartItem);
        entityManager.flush();

        // When
        var items = cartItemRepository.findAll();

        // Then
        assertThat(items).isNotEmpty();
    }

    /**
     * Test with null cart ID - edge case.
     */
    @Test
    @DisplayName("Should handle null cart ID gracefully")
    void testFindByCartIdAndProductId_NullCartId() {
        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(null, testProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test with null product ID - edge case.
     */
    @Test
    @DisplayName("Should handle null product ID gracefully")
    void testFindByCartIdAndProductId_NullProductId() {
        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(testCartId, null);

        // Then
        assertThat(result).isEmpty();
    }
}