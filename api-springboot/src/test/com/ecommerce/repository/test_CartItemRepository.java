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
 * Tests all repository methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
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
     * Creates test cart, product, and cart item entities.
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
        testCartItem.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testCartItem);

        entityManager.flush();
    }

    /**
     * Test finding a cart item by cart ID and product ID.
     * Verifies that the correct cart item is retrieved when both IDs match.
     */
    @Test
    @DisplayName("Should find cart item by cart ID and product ID")
    void testFindByCartIdAndProductId_Success() {
        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        assertThat(result).isPresent();
        assertThat(result.get().getCart().getId()).isEqualTo(cartId);
        assertThat(result.get().getProduct().getProductId()).isEqualTo(productId);
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding a cart item with non-existent cart ID.
     * Verifies that an empty Optional is returned when cart ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when cart ID not found")
    void testFindByCartIdAndProductId_CartNotFound() {
        UUID nonExistentCartId = UUID.randomUUID();

        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, productId);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart item with non-existent product ID.
     * Verifies that an empty Optional is returned when product ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when product ID not found")
    void testFindByCartIdAndProductId_ProductNotFound() {
        UUID nonExistentProductId = UUID.randomUUID();

        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(cartId, nonExistentProductId);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart item with both non-existent IDs.
     * Verifies that an empty Optional is returned when neither ID exists.
     */
    @Test
    @DisplayName("Should return empty when both IDs not found")
    void testFindByCartIdAndProductId_BothNotFound() {
        UUID nonExistentCartId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();

        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(nonExistentCartId, nonExistentProductId);

        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new cart item.
     * Verifies that a cart item can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new cart item successfully")
    void testSaveCartItem() {
        CartItem newCartItem = new CartItem();
        newCartItem.setId(UUID.randomUUID());
        newCartItem.setCart(testCart);
        newCartItem.setProduct(testProduct);
        newCartItem.setQuantity(5);
        newCartItem.setPrice(BigDecimal.valueOf(49.99));

        CartItem saved = cartItemRepository.save(newCartItem);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(5);
        assertThat(saved.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
    }

    /**
     * Test updating an existing cart item.
     * Verifies that cart item quantity can be updated.
     */
    @Test
    @DisplayName("Should update existing cart item")
    void testUpdateCartItem() {
        testCartItem.setQuantity(10);
        CartItem updated = cartItemRepository.save(testCartItem);

        assertThat(updated.getQuantity()).isEqualTo(10);
        assertThat(updated.getId()).isEqualTo(testCartItem.getId());
    }

    /**
     * Test deleting a cart item by ID.
     * Verifies that a cart item can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete cart item by ID")
    void testDeleteCartItem() {
        UUID itemId = testCartItem.getId();
        cartItemRepository.deleteById(itemId);
        entityManager.flush();

        Optional<CartItem> result = cartItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a cart item by ID.
     * Verifies that a cart item can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find cart item by ID")
    void testFindById_Success() {
        Optional<CartItem> result = cartItemRepository.findById(testCartItem.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testCartItem.getId());
    }

    /**
     * Test finding all cart items.
     * Verifies that all cart items can be retrieved.
     */
    @Test
    @DisplayName("Should find all cart items")
    void testFindAll() {
        assertThat(cartItemRepository.findAll()).isNotEmpty();
        assertThat(cartItemRepository.findAll()).hasSize(1);
    }
}