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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for CartItemRepository.
 * Tests repository methods for CartItem entity operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartItemRepository Tests")
public class test_CartItemRepository {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private UUID cartId;
    private UUID productId;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities for Cart, Product, and CartItem.
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
        testProduct.setPrice(99.99);
        entityManager.persist(testProduct);

        testCartItem = new CartItem();
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        entityManager.persist(testCartItem);
        
        entityManager.flush();
    }

    /**
     * Test finding a CartItem by cart ID and product ID when it exists.
     * Verifies that the correct CartItem is returned.
     */
    @Test
    @DisplayName("Should find CartItem by cart ID and product ID when exists")
    void testFindByCartIdAndProductId_WhenExists_ReturnsCartItem() {
        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCart().getId()).isEqualTo(cartId);
        assertThat(result.get().getProduct().getProductId()).isEqualTo(productId);
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding a CartItem by cart ID and product ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when CartItem does not exist")
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
     * Test finding a CartItem with valid cart ID but invalid product ID.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when product ID does not match")
    void testFindByCartIdAndProductId_WhenProductIdNotMatch_ReturnsEmpty() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a CartItem with valid product ID but invalid cart ID.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when cart ID does not match")
    void testFindByCartIdAndProductId_WhenCartIdNotMatch_ReturnsEmpty() {
        // Given
        UUID nonExistentCartId = UUID.randomUUID();

        // When
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, productId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new CartItem.
     * Verifies that the CartItem is persisted correctly.
     */
    @Test
    @DisplayName("Should save new CartItem successfully")
    void testSave_NewCartItem_Success() {
        // Given
        CartItem newCartItem = new CartItem();
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
     * Verifies that the CartItem quantity is updated correctly.
     */
    @Test
    @DisplayName("Should update existing CartItem successfully")
    void testSave_UpdateCartItem_Success() {
        // Given
        testCartItem.setQuantity(10);

        // When
        CartItem updatedCartItem = cartItemRepository.save(testCartItem);

        // Then
        assertThat(updatedCartItem.getQuantity()).isEqualTo(10);
    }

    /**
     * Test deleting a CartItem by ID.
     * Verifies that the CartItem is removed from the database.
     */
    @Test
    @DisplayName("Should delete CartItem by ID successfully")
    void testDeleteById_Success() {
        // Given
        UUID cartItemId = testCartItem.getId();

        // When
        cartItemRepository.deleteById(cartItemId);
        entityManager.flush();

        // Then
        Optional<CartItem> result = cartItemRepository.findById(cartItemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a CartItem by ID.
     * Verifies that the correct CartItem is retrieved.
     */
    @Test
    @DisplayName("Should find CartItem by ID when exists")
    void testFindById_WhenExists_ReturnsCartItem() {
        // Given
        UUID cartItemId = testCartItem.getId();

        // When
        Optional<CartItem> result = cartItemRepository.findById(cartItemId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(cartItemId);
    }
}