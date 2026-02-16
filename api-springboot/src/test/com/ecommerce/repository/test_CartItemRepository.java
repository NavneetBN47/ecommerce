package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for CartItemRepository.
 * Tests repository methods for CartItem entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartItem Repository Tests")
class test_CartItemRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private UUID cartId;
    private UUID productId;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities in the in-memory database.
     */
    @BeforeEach
    void setUp() {
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testCart = new Cart();
        testCart.setId(cartId);
        entityManager.persist(testCart);

        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);

        testCartItem = new CartItem();
        testCartItem.setId(UUID.randomUUID());
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        entityManager.persist(testCartItem);

        entityManager.flush();
    }

    /**
     * Test finding a CartItem by cart ID and product ID.
     * Verifies that the custom query method returns the correct CartItem.
     */
    @Test
    @DisplayName("Should find CartItem by cart ID and product ID")
    void testFindByCartIdAndProductId_Success() {
        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCart().getId()).isEqualTo(cartId);
        assertThat(result.get().getProduct().getProductId()).isEqualTo(productId);
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding a CartItem with non-existent cart ID.
     * Verifies that the method returns empty Optional when cart doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when cart ID does not exist")
    void testFindByCartIdAndProductId_CartNotFound() {
        // Given
        UUID nonExistentCartId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, productId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a CartItem with non-existent product ID.
     * Verifies that the method returns empty Optional when product doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when product ID does not exist")
    void testFindByCartIdAndProductId_ProductNotFound() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a CartItem with both non-existent IDs.
     * Verifies that the method returns empty Optional when neither cart nor product exist.
     */
    @Test
    @DisplayName("Should return empty Optional when both IDs do not exist")
    void testFindByCartIdAndProductId_BothNotFound() {
        // Given
        UUID nonExistentCartId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new CartItem.
     * Verifies that the repository can persist a new CartItem entity.
     */
    @Test
    @DisplayName("Should save a new CartItem successfully")
    void testSave_NewCartItem() {
        // Given
        CartItem newCartItem = new CartItem();
        newCartItem.setId(UUID.randomUUID());
        newCartItem.setCart(testCart);
        newCartItem.setProduct(testProduct);
        newCartItem.setQuantity(5);

        // When
        CartItem savedCartItem = cartItemRepository.save(newCartItem);

        // Then
        assertThat(savedCartItem).isNotNull();
        assertThat(savedCartItem.getId()).isNotNull();
        assertThat(savedCartItem.getQuantity()).isEqualTo(5);
    }

    /**
     * Test updating an existing CartItem.
     * Verifies that the repository can update CartItem properties.
     */
    @Test
    @DisplayName("Should update existing CartItem successfully")
    void testSave_UpdateCartItem() {
        // Given
        testCartItem.setQuantity(10);

        // When
        CartItem updatedCartItem = cartItemRepository.save(testCartItem);

        // Then
        assertThat(updatedCartItem.getQuantity()).isEqualTo(10);
    }

    /**
     * Test finding a CartItem by ID.
     * Verifies that the repository can retrieve a CartItem by its primary key.
     */
    @Test
    @DisplayName("Should find CartItem by ID")
    void testFindById_Success() {
        // When
        Optional<CartItem> result = cartItemRepository.findById(testCartItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testCartItem.getId());
    }

    /**
     * Test deleting a CartItem.
     * Verifies that the repository can delete a CartItem entity.
     */
    @Test
    @DisplayName("Should delete CartItem successfully")
    void testDelete_Success() {
        // Given
        UUID cartItemId = testCartItem.getId();

        // When
        cartItemRepository.delete(testCartItem);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(cartItemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all CartItems.
     * Verifies that the repository can count the total number of CartItems.
     */
    @Test
    @DisplayName("Should count all CartItems")
    void testCount_Success() {
        // When
        long count = cartItemRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if CartItem exists by ID.
     * Verifies that the repository can check existence of a CartItem.
     */
    @Test
    @DisplayName("Should return true when CartItem exists")
    void testExistsById_True() {
        // When
        boolean exists = cartItemRepository.existsById(testCartItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if CartItem exists with non-existent ID.
     * Verifies that the repository returns false for non-existent CartItem.
     */
    @Test
    @DisplayName("Should return false when CartItem does not exist")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = cartItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}