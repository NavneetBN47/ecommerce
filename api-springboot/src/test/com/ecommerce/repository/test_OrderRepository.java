package com.ecommerce.repository;

import com.ecommerce.entity.Order;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for OrderRepository.
 * Tests repository methods for Order entity operations including
 * finding by order number, user ID, status, and order with items.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository Tests")
class test_OrderRepository {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order testOrder;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderNumber("ORD-TEST-001");
        testOrder.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding an order by order number when it exists.
     * Verifies that the correct order is returned.
     */
    @Test
    @DisplayName("Should find order by order number when exists")
    void testFindByOrderNumber_WhenExists_ReturnsOrder() {
        // Given
        Order order = new Order();
        order.setOrderNumber("ORD-12345");
        order.setStatus(Order.OrderStatus.PENDING);
        entityManager.persistAndFlush(order);

        // When
        Optional<Order> result = orderRepository.findByOrderNumber("ORD-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo("ORD-12345");
    }

    /**
     * Test finding an order by order number when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order number does not exist")
    void testFindByOrderNumber_WhenNotExists_ReturnsEmpty() {
        // Given
        String nonExistentOrderNumber = "ORD-NONEXISTENT";

        // When
        Optional<Order> result = orderRepository.findByOrderNumber(nonExistentOrderNumber);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     * Verifies that orders for the specified user are returned.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId_ReturnsUserOrders() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding orders by status.
     * Verifies that only orders with the specified status are returned.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus_ReturnsOrdersWithStatus() {
        // Given
        Order order1 = new Order();
        order1.setOrderNumber("ORD-001");
        order1.setStatus(Order.OrderStatus.PENDING);
        
        Order order2 = new Order();
        order2.setOrderNumber("ORD-002");
        order2.setStatus(Order.OrderStatus.COMPLETED);
        
        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.flush();

        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).allMatch(order -> order.getStatus() == Order.OrderStatus.PENDING);
    }

    /**
     * Test finding order by ID with items.
     * Verifies that the order is returned with its items loaded.
     */
    @Test
    @DisplayName("Should find order by ID with items")
    void testFindByIdWithItems_ReturnsOrderWithItems() {
        // Given
        Order order = new Order();
        order.setOrderNumber("ORD-WITH-ITEMS");
        order.setStatus(Order.OrderStatus.PENDING);
        Order savedOrder = entityManager.persistAndFlush(order);

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test checking if order number exists.
     * Verifies that the existence check returns true for existing order numbers.
     */
    @Test
    @DisplayName("Should check if order number exists")
    void testExistsByOrderNumber_WhenExists_ReturnsTrue() {
        // Given
        Order order = new Order();
        order.setOrderNumber("ORD-EXISTS");
        order.setStatus(Order.OrderStatus.PENDING);
        entityManager.persistAndFlush(order);

        // When
        boolean exists = orderRepository.existsByOrderNumber("ORD-EXISTS");

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order number exists when it doesn't.
     * Verifies that the existence check returns false for non-existent order numbers.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_WhenNotExists_ReturnsFalse() {
        // Given
        String nonExistentOrderNumber = "ORD-NOT-EXISTS";

        // When
        boolean exists = orderRepository.existsByOrderNumber(nonExistentOrderNumber);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving an order.
     * Verifies that the order is persisted correctly.
     */
    @Test
    @DisplayName("Should save order successfully")
    void testSave_ValidOrder_SavesSuccessfully() {
        // Given
        Order newOrder = new Order();
        newOrder.setOrderNumber("ORD-NEW");
        newOrder.setStatus(Order.OrderStatus.PENDING);

        // When
        Order savedOrder = orderRepository.save(newOrder);

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderNumber()).isEqualTo("ORD-NEW");
    }

    /**
     * Test finding an order by ID.
     * Verifies that the order can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find order by ID when exists")
    void testFindById_WhenExists_ReturnsOrder() {
        // Given
        Order order = new Order();
        order.setOrderNumber("ORD-FIND");
        order.setStatus(Order.OrderStatus.PENDING);
        Order savedOrder = entityManager.persistAndFlush(order);

        // When
        Optional<Order> result = orderRepository.findById(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());
    }

    /**
     * Test deleting an order.
     * Verifies that the order is removed from the database.
     */
    @Test
    @DisplayName("Should delete order successfully")
    void testDelete_ExistingOrder_DeletesSuccessfully() {
        // Given
        Order order = new Order();
        order.setOrderNumber("ORD-DELETE");
        order.setStatus(Order.OrderStatus.PENDING);
        Order savedOrder = entityManager.persistAndFlush(order);
        Long orderId = savedOrder.getId();

        // When
        orderRepository.deleteById(orderId);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(orderId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting orders.
     * Verifies that the count of orders is correct.
     */
    @Test
    @DisplayName("Should count orders correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = orderRepository.count();
        Order order = new Order();
        order.setOrderNumber("ORD-COUNT");
        order.setStatus(Order.OrderStatus.PENDING);
        entityManager.persistAndFlush(order);

        // When
        long newCount = orderRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
}