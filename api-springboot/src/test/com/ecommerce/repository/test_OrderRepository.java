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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for OrderRepository.
 * Tests all repository methods including custom query methods for order operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository Tests")
public class test_OrderRepository {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Order testOrder;
    private Product testProduct;
    private String testOrderNumber;

    /**
     * Set up test data before each test method execution.
     * Creates test user, order, and product entities.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        testOrderNumber = "ORD-" + UUID.randomUUID().toString();
        testOrder = new Order();
        testOrder.setOrderNumber(testOrderNumber);
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(199.98));
        entityManager.persist(testOrder);

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        orderItem.setPrice(BigDecimal.valueOf(99.99));
        orderItem.setSubtotal(BigDecimal.valueOf(199.98));
        entityManager.persist(orderItem);

        entityManager.flush();
    }

    /**
     * Test finding an order by order number.
     * Verifies that the correct order is retrieved by its order number.
     */
    @Test
    @DisplayName("Should find order by order number")
    void testFindByOrderNumber_Success() {
        Optional<Order> result = orderRepository.findByOrderNumber(testOrderNumber);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
        assertThat(result.get().getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding an order with non-existent order number.
     * Verifies that an empty Optional is returned when order number doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when order number not found")
    void testFindByOrderNumber_NotFound() {
        Optional<Order> result = orderRepository.findByOrderNumber("NON-EXISTENT");

        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     * Verifies that all orders for a user are retrieved with pagination.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> result = orderRepository.findByUserId(testUser.getId(), pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding orders by non-existent user ID.
     * Verifies that an empty page is returned when user ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty page when user ID not found")
    void testFindByUserId_NotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> result = orderRepository.findByUserId(999L, pageable);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with multiple pages.
     * Verifies pagination works correctly with multiple orders.
     */
    @Test
    @DisplayName("Should handle pagination correctly with multiple orders")
    void testFindByUserId_Pagination() {
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderNumber("ORD-" + UUID.randomUUID().toString());
            order.setUser(testUser);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(BigDecimal.valueOf(100.00));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 3);
        Page<Order> result = orderRepository.findByUserId(testUser.getId(), pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    /**
     * Test finding orders by status.
     * Verifies that orders with specific status are retrieved.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus_Success() {
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding orders by status when none exist.
     * Verifies that an empty list is returned when no orders have the specified status.
     */
    @Test
    @DisplayName("Should return empty list when no orders with status")
    void testFindByStatus_NotFound() {
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by multiple statuses.
     * Verifies that orders with different statuses can be retrieved.
     */
    @Test
    @DisplayName("Should find orders with different statuses")
    void testFindByStatus_MultipleStatuses() {
        Order shippedOrder = new Order();
        shippedOrder.setOrderNumber("ORD-SHIPPED");
        shippedOrder.setUser(testUser);
        shippedOrder.setStatus(Order.OrderStatus.SHIPPED);
        shippedOrder.setTotalAmount(BigDecimal.valueOf(150.00));
        entityManager.persist(shippedOrder);
        entityManager.flush();

        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        List<Order> shippedOrders = orderRepository.findByStatus(Order.OrderStatus.SHIPPED);

        assertThat(pendingOrders).hasSize(1);
        assertThat(shippedOrders).hasSize(1);
    }

    /**
     * Test finding an order by ID with items.
     * Verifies that order with its items is retrieved using fetch join.
     */
    @Test
    @DisplayName("Should find order by ID with items")
    void testFindByIdWithItems_Success() {
        Optional<Order> result = orderRepository.findByIdWithItems(testOrder.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testOrder.getId());
        assertThat(result.get().getItems()).isNotEmpty();
    }

    /**
     * Test finding an order with items using non-existent ID.
     * Verifies that an empty Optional is returned when order ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when order ID not found with items")
    void testFindByIdWithItems_NotFound() {
        Optional<Order> result = orderRepository.findByIdWithItems(999L);

        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order number exists.
     * Verifies that existence check returns true for existing order number.
     */
    @Test
    @DisplayName("Should return true when order number exists")
    void testExistsByOrderNumber_Exists() {
        boolean exists = orderRepository.existsByOrderNumber(testOrderNumber);

        assertThat(exists).isTrue();
    }

    /**
     * Test checking if non-existent order number exists.
     * Verifies that existence check returns false for non-existent order number.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_NotExists() {
        boolean exists = orderRepository.existsByOrderNumber("NON-EXISTENT");

        assertThat(exists).isFalse();
    }

    /**
     * Test saving a new order.
     * Verifies that an order can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new order successfully")
    void testSaveOrder() {
        Order newOrder = new Order();
        newOrder.setOrderNumber("ORD-NEW");
        newOrder.setUser(testUser);
        newOrder.setStatus(Order.OrderStatus.PENDING);
        newOrder.setTotalAmount(BigDecimal.valueOf(299.99));

        Order saved = orderRepository.save(newOrder);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderNumber()).isEqualTo("ORD-NEW");
    }

    /**
     * Test updating an existing order.
     * Verifies that order status can be updated.
     */
    @Test
    @DisplayName("Should update existing order")
    void testUpdateOrder() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        Order updated = orderRepository.save(testOrder);

        assertThat(updated.getStatus()).isEqualTo(Order.OrderStatus.SHIPPED);
        assertThat(updated.getId()).isEqualTo(testOrder.getId());
    }

    /**
     * Test deleting an order by ID.
     * Verifies that an order can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete order by ID")
    void testDeleteOrder() {
        Long orderId = testOrder.getId();
        orderRepository.deleteById(orderId);
        entityManager.flush();

        Optional<Order> result = orderRepository.findById(orderId);
        assertThat(result).isEmpty();
    }
}