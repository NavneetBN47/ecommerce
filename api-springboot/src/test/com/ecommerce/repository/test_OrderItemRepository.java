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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for OrderItemRepository.
 * Tests all repository methods for order item operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
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
    private User testUser;

    /**
     * Set up test data before each test method execution.
     * Creates test user, order, product, and order item entities.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        testOrder = new Order();
        testOrder.setOrderNumber("ORD-" + UUID.randomUUID().toString());
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(199.98));
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
     * Test saving a new order item.
     * Verifies that an order item can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new order item successfully")
    void testSaveOrderItem() {
        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setOrder(testOrder);
        newOrderItem.setProduct(testProduct);
        newOrderItem.setQuantity(3);
        newOrderItem.setPrice(BigDecimal.valueOf(49.99));
        newOrderItem.setSubtotal(BigDecimal.valueOf(149.97));

        OrderItem saved = orderItemRepository.save(newOrderItem);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(3);
        assertThat(saved.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(saved.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(149.97));
    }

    /**
     * Test finding an order item by ID.
     * Verifies that an order item can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find order item by ID")
    void testFindById_Success() {
        Optional<OrderItem> result = orderItemRepository.findById(testOrderItem.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testOrderItem.getId());
        assertThat(result.get().getQuantity()).isEqualTo(2);
        assertThat(result.get().getProduct().getProductId()).isEqualTo(testProduct.getProductId());
    }

    /**
     * Test finding an order item with non-existent ID.
     * Verifies that an empty Optional is returned when ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when order item ID not found")
    void testFindById_NotFound() {
        Optional<OrderItem> result = orderItemRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    /**
     * Test updating an existing order item.
     * Verifies that order item quantity can be updated.
     */
    @Test
    @DisplayName("Should update existing order item")
    void testUpdateOrderItem() {
        testOrderItem.setQuantity(5);
        testOrderItem.setSubtotal(BigDecimal.valueOf(499.95));
        OrderItem updated = orderItemRepository.save(testOrderItem);

        assertThat(updated.getQuantity()).isEqualTo(5);
        assertThat(updated.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(499.95));
        assertThat(updated.getId()).isEqualTo(testOrderItem.getId());
    }

    /**
     * Test deleting an order item by ID.
     * Verifies that an order item can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete order item by ID")
    void testDeleteOrderItem() {
        Long itemId = testOrderItem.getId();
        orderItemRepository.deleteById(itemId);
        entityManager.flush();

        Optional<OrderItem> result = orderItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all order items.
     * Verifies that all order items can be retrieved.
     */
    @Test
    @DisplayName("Should find all order items")
    void testFindAll() {
        List<OrderItem> allItems = orderItemRepository.findAll();

        assertThat(allItems).isNotEmpty();
        assertThat(allItems).hasSize(1);
        assertThat(allItems.get(0).getId()).isEqualTo(testOrderItem.getId());
    }

    /**
     * Test saving multiple order items for the same order.
     * Verifies that multiple items can be associated with one order.
     */
    @Test
    @DisplayName("Should save multiple order items for same order")
    void testSaveMultipleOrderItems() {
        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(29.99));
        entityManager.persist(product2);

        OrderItem item2 = new OrderItem();
        item2.setOrder(testOrder);
        item2.setProduct(product2);
        item2.setQuantity(1);
        item2.setPrice(BigDecimal.valueOf(29.99));
        item2.setSubtotal(BigDecimal.valueOf(29.99));
        orderItemRepository.save(item2);

        List<OrderItem> allItems = orderItemRepository.findAll();
        assertThat(allItems).hasSize(2);
    }

    /**
     * Test order item with zero quantity.
     * Verifies that order items with zero quantity can be persisted.
     */
    @Test
    @DisplayName("Should handle order item with zero quantity")
    void testOrderItemWithZeroQuantity() {
        OrderItem zeroQtyItem = new OrderItem();
        zeroQtyItem.setOrder(testOrder);
        zeroQtyItem.setProduct(testProduct);
        zeroQtyItem.setQuantity(0);
        zeroQtyItem.setPrice(BigDecimal.valueOf(99.99));
        zeroQtyItem.setSubtotal(BigDecimal.ZERO);

        OrderItem saved = orderItemRepository.save(zeroQtyItem);

        assertThat(saved.getQuantity()).isEqualTo(0);
        assertThat(saved.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Test deleting all order items.
     * Verifies that all order items can be deleted at once.
     */
    @Test
    @DisplayName("Should delete all order items")
    void testDeleteAll() {
        orderItemRepository.deleteAll();
        entityManager.flush();

        List<OrderItem> allItems = orderItemRepository.findAll();
        assertThat(allItems).isEmpty();
    }

    /**
     * Test counting order items.
     * Verifies that the count of order items is accurate.
     */
    @Test
    @DisplayName("Should count order items correctly")
    void testCount() {
        long count = orderItemRepository.count();

        assertThat(count).isEqualTo(1L);
    }
}