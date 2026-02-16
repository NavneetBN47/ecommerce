package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
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
 * JUnit test class for OrderItemRepository.
 * Tests repository methods for OrderItem entity operations.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderItem Repository Tests")
class test_OrderItemRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities in the in-memory database.
     */
    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        entityManager.persist(testUser);

        testOrder = new Order();
        testOrder.setOrderNumber("ORD-" + System.currentTimeMillis());
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(199.99));
        entityManager.persist(testOrder);

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);

        testOrderItem = new OrderItem();
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(BigDecimal.valueOf(99.99));
        testOrderItem.setSubtotal(BigDecimal.valueOf(199.98));
        entityManager.persist(testOrderItem);

        entityManager.flush();
    }

    /**
     * Test saving a new OrderItem.
     * Verifies that the repository can persist a new OrderItem entity.
     */
    @Test
    @DisplayName("Should save a new OrderItem successfully")
    void testSave_NewOrderItem() {
        // Given
        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setOrder(testOrder);
        newOrderItem.setProduct(testProduct);
        newOrderItem.setQuantity(5);
        newOrderItem.setPrice(BigDecimal.valueOf(99.99));
        newOrderItem.setSubtotal(BigDecimal.valueOf(499.95));

        // When
        OrderItem savedOrderItem = orderItemRepository.save(newOrderItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(5);
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(499.95));
    }

    /**
     * Test updating an existing OrderItem.
     * Verifies that the repository can update OrderItem properties.
     */
    @Test
    @DisplayName("Should update existing OrderItem successfully")
    void testSave_UpdateOrderItem() {
        // Given
        testOrderItem.setQuantity(10);
        testOrderItem.setSubtotal(BigDecimal.valueOf(999.90));

        // When
        OrderItem updatedOrderItem = orderItemRepository.save(testOrderItem);

        // Then
        assertThat(updatedOrderItem.getQuantity()).isEqualTo(10);
        assertThat(updatedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(999.90));
    }

    /**
     * Test finding an OrderItem by ID.
     * Verifies that the repository can retrieve an OrderItem by its primary key.
     */
    @Test
    @DisplayName("Should find OrderItem by ID")
    void testFindById_Success() {
        // When
        Optional<OrderItem> result = orderItemRepository.findById(testOrderItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testOrderItem.getId());
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding an OrderItem with non-existent ID.
     * Verifies that the method returns empty Optional for non-existent OrderItem.
     */
    @Test
    @DisplayName("Should return empty Optional when OrderItem ID does not exist")
    void testFindById_NotFound() {
        // Given
        Long nonExistentId = 99999L;

        // When
        Optional<OrderItem> result = orderItemRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an OrderItem.
     * Verifies that the repository can delete an OrderItem entity.
     */
    @Test
    @DisplayName("Should delete OrderItem successfully")
    void testDelete_Success() {
        // Given
        Long orderItemId = testOrderItem.getId();

        // When
        orderItemRepository.delete(testOrderItem);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(orderItemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an OrderItem by ID.
     * Verifies that the repository can delete an OrderItem by its primary key.
     */
    @Test
    @DisplayName("Should delete OrderItem by ID successfully")
    void testDeleteById_Success() {
        // Given
        Long orderItemId = testOrderItem.getId();

        // When
        orderItemRepository.deleteById(orderItemId);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(orderItemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all OrderItems.
     * Verifies that the repository can count the total number of OrderItems.
     */
    @Test
    @DisplayName("Should count all OrderItems")
    void testCount_Success() {
        // When
        long count = orderItemRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if OrderItem exists by ID.
     * Verifies that the repository can check existence of an OrderItem.
     */
    @Test
    @DisplayName("Should return true when OrderItem exists")
    void testExistsById_True() {
        // When
        boolean exists = orderItemRepository.existsById(testOrderItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if OrderItem exists with non-existent ID.
     * Verifies that the repository returns false for non-existent OrderItem.
     */
    @Test
    @DisplayName("Should return false when OrderItem does not exist")
    void testExistsById_False() {
        // Given
        Long nonExistentId = 99999L;

        // When
        boolean exists = orderItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding all OrderItems.
     * Verifies that the repository can retrieve all OrderItems.
     */
    @Test
    @DisplayName("Should find all OrderItems")
    void testFindAll_Success() {
        // When
        var result = orderItemRepository.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    /**
     * Test saving OrderItem with zero quantity.
     * Verifies that the repository can handle edge case of zero quantity.
     */
    @Test
    @DisplayName("Should save OrderItem with zero quantity")
    void testSave_ZeroQuantity() {
        // Given
        OrderItem zeroQuantityItem = new OrderItem();
        zeroQuantityItem.setOrder(testOrder);
        zeroQuantityItem.setProduct(testProduct);
        zeroQuantityItem.setQuantity(0);
        zeroQuantityItem.setPrice(BigDecimal.valueOf(99.99));
        zeroQuantityItem.setSubtotal(BigDecimal.ZERO);

        // When
        OrderItem savedOrderItem = orderItemRepository.save(zeroQuantityItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(0);
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Test saving OrderItem with large quantity.
     * Verifies that the repository can handle large quantity values.
     */
    @Test
    @DisplayName("Should save OrderItem with large quantity")
    void testSave_LargeQuantity() {
        // Given
        OrderItem largeQuantityItem = new OrderItem();
        largeQuantityItem.setOrder(testOrder);
        largeQuantityItem.setProduct(testProduct);
        largeQuantityItem.setQuantity(1000);
        largeQuantityItem.setPrice(BigDecimal.valueOf(99.99));
        largeQuantityItem.setSubtotal(BigDecimal.valueOf(99990.00));

        // When
        OrderItem savedOrderItem = orderItemRepository.save(largeQuantityItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(1000);
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(99990.00));
    }

    /**
     * Test saving OrderItem with decimal price.
     * Verifies that the repository correctly handles decimal prices.
     */
    @Test
    @DisplayName("Should save OrderItem with decimal price")
    void testSave_DecimalPrice() {
        // Given
        OrderItem decimalPriceItem = new OrderItem();
        decimalPriceItem.setOrder(testOrder);
        decimalPriceItem.setProduct(testProduct);
        decimalPriceItem.setQuantity(3);
        decimalPriceItem.setPrice(BigDecimal.valueOf(19.99));
        decimalPriceItem.setSubtotal(BigDecimal.valueOf(59.97));

        // When
        OrderItem savedOrderItem = orderItemRepository.save(decimalPriceItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(59.97));
    }
}