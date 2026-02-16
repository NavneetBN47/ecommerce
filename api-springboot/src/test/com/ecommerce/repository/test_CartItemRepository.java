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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
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
        testCartItem.setCartId(testCartId);
        testCartItem.setProductId(testProductId);
        testCartItem.setQuantity(2);
    }

    /**
     * Test finding cart item by cart ID and product ID when item exists.
     * Verifies that the correct cart item is returned.
     */
    @Test
    @DisplayName("Should find cart item by cart ID and product ID when exists")
    void testFindByCartIdAndProductId_WhenExists() {
        // Given
        entityManager.persist(testCartItem);
        entityManager.flush();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(testCartId, testProductId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCartId()).isEqualTo(testCartId);
        assertThat(result.get().getProductId()).isEqualTo(testProductId);
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding cart item by cart ID and product ID when item does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when cart item not found")
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
     * Test saving a new cart item.
     * Verifies that the cart item is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new cart item successfully")
    void testSave_NewCartItem() {
        // When
        CartItem savedCartItem = cartItemRepository.save(testCartItem);

        // Then
        assertThat(savedCartItem).isNotNull();
        assertThat(savedCartItem.getId()).isNotNull();
        assertThat(savedCartItem.getCartId()).isEqualTo(testCartId);
        assertThat(savedCartItem.getProductId()).isEqualTo(testProductId);
    }

    /**
     * Test updating an existing cart item.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing cart item successfully")
    void testSave_UpdateCartItem() {
        // Given
        CartItem savedCartItem = entityManager.persist(testCartItem);
        entityManager.flush();
        UUID savedId = savedCartItem.getId();

        // When
        savedCartItem.setQuantity(5);
        CartItem updatedCartItem = cartItemRepository.save(savedCartItem);

        // Then
        assertThat(updatedCartItem.getId()).isEqualTo(savedId);
        assertThat(updatedCartItem.getQuantity()).isEqualTo(5);
    }

    /**
     * Test finding cart item by ID.
     * Verifies that the correct cart item is retrieved.
     */
    @Test
    @DisplayName("Should find cart item by ID when exists")
    void testFindById_WhenExists() {
        // Given
        CartItem savedCartItem = entityManager.persist(testCartItem);
        entityManager.flush();

        // When
        Optional<CartItem> result = cartItemRepository.findById(savedCartItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCartItem.getId());
    }

    /**
     * Test deleting a cart item by ID.
     * Verifies that the cart item is removed from the database.
     */
    @Test
    @DisplayName("Should delete cart item by ID successfully")
    void testDeleteById() {
        // Given
        CartItem savedCartItem = entityManager.persist(testCartItem);
        entityManager.flush();
        UUID savedId = savedCartItem.getId();

        // When
        cartItemRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all cart items.
     * Verifies that all persisted cart items are retrieved.
     */
    @Test
    @DisplayName("Should find all cart items")
    void testFindAll() {
        // Given
        entityManager.persist(testCartItem);
        
        CartItem anotherCartItem = new CartItem();
        anotherCartItem.setCartId(UUID.randomUUID());
        anotherCartItem.setProductId(UUID.randomUUID());
        anotherCartItem.setQuantity(3);
        entityManager.persist(anotherCartItem);
        entityManager.flush();

        // When
        var allCartItems = cartItemRepository.findAll();

        // Then
        assertThat(allCartItems).hasSize(2);
    }

    /**
     * Test checking if cart item exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when cart item exists by ID")
    void testExistsById_WhenExists() {
        // Given
        CartItem savedCartItem = entityManager.persist(testCartItem);
        entityManager.flush();

        // When
        boolean exists = cartItemRepository.existsById(savedCartItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if cart item exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when cart item does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = cartItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}