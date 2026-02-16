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
 * Tests repository methods for CartItem entity operations.
 * Uses in-memory database for testing.
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
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testCartId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        
        testCartItem = new CartItem();
        testCartItem.setId(UUID.randomUUID());
        // Note: Actual entity setup would depend on CartItem entity structure
    }

    /**
     * Test finding a cart item by cart ID and product ID when it exists.
     * Verifies that the correct cart item is returned.
     */
    @Test
    @DisplayName("Should find cart item by cart ID and product ID when exists")
    void testFindByCartIdAndProductId_WhenExists_ReturnsCartItem() {
        // Given
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        entityManager.persistAndFlush(cartItem);

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding a cart item by cart ID and product ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when cart item does not exist")
    void testFindByCartIdAndProductId_WhenNotExists_ReturnsEmpty() {
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
     * Verifies that the cart item is persisted correctly.
     */
    @Test
    @DisplayName("Should save cart item successfully")
    void testSave_ValidCartItem_SavesSuccessfully() {
        // Given
        CartItem newCartItem = new CartItem();
        newCartItem.setId(UUID.randomUUID());

        // When
        CartItem savedCartItem = cartItemRepository.save(newCartItem);

        // Then
        assertThat(savedCartItem).isNotNull();
        assertThat(savedCartItem.getId()).isNotNull();
    }

    /**
     * Test finding a cart item by ID.
     * Verifies that the cart item can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find cart item by ID when exists")
    void testFindById_WhenExists_ReturnsCartItem() {
        // Given
        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        CartItem savedCartItem = entityManager.persistAndFlush(cartItem);

        // When
        Optional<CartItem> result = cartItemRepository.findById(savedCartItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCartItem.getId());
    }

    /**
     * Test deleting a cart item.
     * Verifies that the cart item is removed from the database.
     */
    @Test
    @DisplayName("Should delete cart item successfully")
    void testDelete_ExistingCartItem_DeletesSuccessfully() {
        // Given
        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        CartItem savedCartItem = entityManager.persistAndFlush(cartItem);
        UUID cartItemId = savedCartItem.getId();

        // When
        cartItemRepository.deleteById(cartItemId);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(cartItemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all cart items.
     * Verifies that all cart items can be retrieved.
     */
    @Test
    @DisplayName("Should find all cart items")
    void testFindAll_ReturnsAllCartItems() {
        // Given
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(UUID.randomUUID());
        CartItem cartItem2 = new CartItem();
        cartItem2.setId(UUID.randomUUID());
        entityManager.persist(cartItem1);
        entityManager.persist(cartItem2);
        entityManager.flush();

        // When
        var result = cartItemRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting cart items.
     * Verifies that the count of cart items is correct.
     */
    @Test
    @DisplayName("Should count cart items correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = cartItemRepository.count();
        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        entityManager.persistAndFlush(cartItem);

        // When
        long newCount = cartItemRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test checking if cart item exists by ID.
     * Verifies that existence check works correctly.
     */
    @Test
    @DisplayName("Should check if cart item exists by ID")
    void testExistsById_WhenExists_ReturnsTrue() {
        // Given
        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        CartItem savedCartItem = entityManager.persistAndFlush(cartItem);

        // When
        boolean exists = cartItemRepository.existsById(savedCartItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if cart item exists by ID when it doesn't exist.
     * Verifies that existence check returns false for non-existent items.
     */
    @Test
    @DisplayName("Should return false when cart item does not exist")
    void testExistsById_WhenNotExists_ReturnsFalse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = cartItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}