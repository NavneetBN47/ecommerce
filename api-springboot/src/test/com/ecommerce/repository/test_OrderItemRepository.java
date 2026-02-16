package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
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
 * JUnit 5 test class for OrderItemRepository.
 * Tests repository methods for OrderItem entity operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderItemRepository Tests")
public class test_OrderItemRepository {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities for Order, Product, and OrderItem.
     */
    @BeforeEach
    void setUp() {
        // Create test order
        testOrder = new Order();
        testOrder.setOrderNumber("ORD-" + UUID.randomUUID().toString());
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(299.99));
        entityManager.persist(testOrder);

        // Create test product
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        entityManager.persist(testProduct);

        // Create test order item
        testOrderItem = new OrderItem();
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(3);
        testOrderItem.setPrice(BigDecimal.valueOf(99.99));
        testOrderItem.setSubtotal(BigDecimal.valueOf(299.97));
        entityManager.persist(testOrderItem);
        
        entityManager.flush();
    }

    /**
     * Test saving a new OrderItem.
     * Verifies that the OrderItem is persisted correctly with all attributes.
     */
    @Test
    @DisplayName("Should save new OrderItem successfully")
    void testSave_NewOrderItem_Success() {
        // Given
        Product anotherProduct = new Product();
        anotherProduct.setProductId(UUID.randomUUID());
        anotherProduct.setName("Another Product");
        anotherProduct.setPrice(49.99);
        entityManager.persist(anotherProduct);

        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setOrder(testOrder);
        newOrderItem.setProduct(anotherProduct);
        newOrderItem.setQuantity(2);
        newOrderItem.setPrice(BigDecimal.valueOf(49.99));
        newOrderItem.setSubtotal(BigDecimal.valueOf(99.98));

        // When
        OrderItem savedOrderItem = orderItemRepository.save(newOrderItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(2);
        assertThat(savedOrderItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(99.98));
    }

    /**
     * Test updating an existing OrderItem.
     * Verifies that OrderItem modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing OrderItem successfully")
    void testSave_UpdateOrderItem_Success() {
        // Given
        testOrderItem.setQuantity(5);
        testOrderItem.setSubtotal(BigDecimal.valueOf(499.95));

        // When
        OrderItem updatedOrderItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // Then
        assertThat(updatedOrderItem.getQuantity()).isEqualTo(5);
        assertThat(updatedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(499.95));
    }

    /**
     * Test finding an OrderItem by ID.
     * Verifies that the correct OrderItem is retrieved.
     */
    @Test
    @DisplayName("Should find OrderItem by ID when exists")
    void testFindById_WhenExists_ReturnsOrderItem() {
        // Given
        Long orderItemId = testOrderItem.getId();

        // When
        Optional<OrderItem> result = orderItemRepository.findById(orderItemId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(orderItemId);
        assertThat(result.get().getQuantity()).isEqualTo(3);
        assertThat(result.get().getProduct().getName()).isEqualTo("Test Product");
    }

    /**
     * Test finding an OrderItem by ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when OrderItem ID does not exist")
    void testFindById_WhenNotExists_ReturnsEmpty() {
        // Given
        Long nonExistentId = 99999L;

        // When
        Optional<OrderItem> result = orderItemRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an OrderItem by ID.
     * Verifies that the OrderItem is removed from the database.
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
     * Test finding all OrderItems.
     * Verifies that all persisted OrderItems are retrieved.
     */
    @Test
    @DisplayName("Should find all OrderItems")
    void testFindAll_ReturnsAllOrderItems() {
        // When
        var allOrderItems = orderItemRepository.findAll();

        // Then
        assertThat(allOrderItems).isNotEmpty();
        assertThat(allOrderItems).hasSize(1);
        assertThat(allOrderItems.get(0).getId()).isEqualTo(testOrderItem.getId());
    }

    /**
     * Test saving OrderItem with zero quantity.
     * Verifies that edge case values are handled correctly.
     */
    @Test
    @DisplayName("Should save OrderItem with zero quantity")
    void testSave_WithZeroQuantity_Success() {
        // Given
        testOrderItem.setQuantity(0);
        testOrderItem.setSubtotal(BigDecimal.ZERO);

        // When
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);

        // Then
        assertThat(savedOrderItem.getQuantity()).isEqualTo(0);
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Test saving OrderItem with large quantity.
     * Verifies that large values are handled correctly.
     */
    @Test
    @DisplayName("Should save OrderItem with large quantity")
    void testSave_WithLargeQuantity_Success() {
        // Given
        testOrderItem.setQuantity(1000);
        testOrderItem.setSubtotal(BigDecimal.valueOf(99990.00));

        // When
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);

        // Then
        assertThat(savedOrderItem.getQuantity()).isEqualTo(1000);
        assertThat(savedOrderItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(99990.00));
    }

    /**
     * Test that OrderItem maintains relationship with Order.
     * Verifies bidirectional relationship integrity.
     */
    @Test
    @DisplayName("Should maintain relationship with Order")
    void testOrderItemOrderRelationship() {
        // When
        Optional<OrderItem> result = orderItemRepository.findById(testOrderItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrder()).isNotNull();
        assertThat(result.get().getOrder().getId()).isEqualTo(testOrder.getId());
        assertThat(result.get().getOrder().getOrderNumber()).isEqualTo(testOrder.getOrderNumber());
    }

    /**
     * Test that OrderItem maintains relationship with Product.
     * Verifies bidirectional relationship integrity.
     */
    @Test
    @DisplayName("Should maintain relationship with Product")
    void testOrderItemProductRelationship() {
        // When
        Optional<OrderItem> result = orderItemRepository.findById(testOrderItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProduct()).isNotNull();
        assertThat(result.get().getProduct().getProductId()).isEqualTo(testProduct.getProductId());
        assertThat(result.get().getProduct().getName()).isEqualTo("Test Product");
    }
}