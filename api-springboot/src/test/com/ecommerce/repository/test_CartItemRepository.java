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
 * Tests repository methods for CartItem entity operations.
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
    }

    /**
     * Test finding a cart item by cart ID and product ID when it exists.
     * Verifies that the repository correctly retrieves an existing cart item.
     */
    @Test
    @DisplayName("Should find cart item by cart ID and product ID when exists")
    void testFindByCartIdAndProductId_WhenExists() {
        // Given
        CartItem savedItem = entityManager.persistAndFlush(testCartItem);

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(testCartId, testProductId);

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding a cart item by cart ID and product ID when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent items.
     */
    @Test
    @DisplayName("Should return empty when cart item does not exist")
    void testFindByCartIdAndProductId_WhenNotExists() {
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
     * Verifies that the repository correctly persists a cart item.
     */
    @Test
    @DisplayName("Should save cart item successfully")
    void testSave_CartItem() {
        // When
        CartItem savedItem = cartItemRepository.save(testCartItem);

        // Then
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
    }

    /**
     * Test finding a cart item by ID.
     * Verifies that the repository correctly retrieves a cart item by its ID.
     */
    @Test
    @DisplayName("Should find cart item by ID")
    void testFindById_WhenExists() {
        // Given
        CartItem savedItem = entityManager.persistAndFlush(testCartItem);

        // When
        Optional<CartItem> result = cartItemRepository.findById(savedItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedItem.getId());
    }

    /**
     * Test deleting a cart item.
     * Verifies that the repository correctly deletes a cart item.
     */
    @Test
    @DisplayName("Should delete cart item successfully")
    void testDelete_CartItem() {
        // Given
        CartItem savedItem = entityManager.persistAndFlush(testCartItem);
        UUID itemId = savedItem.getId();

        // When
        cartItemRepository.delete(savedItem);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all cart items.
     * Verifies that the repository correctly retrieves all cart items.
     */
    @Test
    @DisplayName("Should find all cart items")
    void testFindAll_CartItems() {
        // Given
        entityManager.persistAndFlush(testCartItem);
        CartItem anotherItem = new CartItem();
        anotherItem.setId(UUID.randomUUID());
        entityManager.persistAndFlush(anotherItem);

        // When
        var allItems = cartItemRepository.findAll();

        // Then
        assertThat(allItems).hasSizeGreaterThanOrEqualTo(2);
    }
}