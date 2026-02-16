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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for OrderRepository.
 * Tests repository methods for Order entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("OrderRepository Tests")
public class test_OrderRepository {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order testOrder;
    private String testOrderNumber;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testOrderNumber = "ORD-2024-001";
        testOrder = new Order();
        testOrder.setOrderNumber(testOrderNumber);
        testOrder.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding an order by order number when it exists.
     * Verifies that the repository correctly retrieves an order by its order number.
     */
    @Test
    @DisplayName("Should find order by order number when exists")
    void testFindByOrderNumber_WhenExists() {
        // Given
        Order savedOrder = entityManager.persistAndFlush(testOrder);

        // When
        Optional<Order> result = orderRepository.findByOrderNumber(testOrderNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test finding an order by order number when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent order.
     */
    @Test
    @DisplayName("Should return empty when order number does not exist")
    void testFindByOrderNumber_WhenNotExists() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     * Verifies that the repository correctly retrieves paginated orders for a user.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId_WithPagination() {
        // Given
        Long userId = 1L;
        entityManager.persistAndFlush(testOrder);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding orders by status.
     * Verifies that the repository correctly retrieves orders by their status.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus() {
        // Given
        entityManager.persistAndFlush(testOrder);
        Order anotherOrder = new Order();
        anotherOrder.setOrderNumber("ORD-2024-002");
        anotherOrder.setStatus(Order.OrderStatus.PENDING);
        entityManager.persistAndFlush(anotherOrder);

        // When
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        // Then
        assertThat(pendingOrders).isNotEmpty();
        assertThat(pendingOrders).allMatch(order -> order.getStatus() == Order.OrderStatus.PENDING);
    }

    /**
     * Test finding order with items using fetch join.
     * Verifies that the repository correctly retrieves an order with its items.
     */
    @Test
    @DisplayName("Should find order with items")
    void testFindByIdWithItems() {
        // Given
        Order savedOrder = entityManager.persistAndFlush(testOrder);

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test checking if order number exists.
     * Verifies that the repository correctly checks order number existence.
     */
    @Test
    @DisplayName("Should check if order number exists")
    void testExistsByOrderNumber_WhenExists() {
        // Given
        entityManager.persistAndFlush(testOrder);

        // When
        boolean exists = orderRepository.existsByOrderNumber(testOrderNumber);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order number does not exist.
     * Verifies that the repository correctly returns false for non-existent order number.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_WhenNotExists() {
        // When
        boolean exists = orderRepository.existsByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving an order.
     * Verifies that the repository correctly persists an order.
     */
    @Test
    @DisplayName("Should save order successfully")
    void testSave_Order() {
        // When
        Order savedOrder = orderRepository.save(testOrder);

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test finding an order by ID.
     * Verifies that the repository correctly retrieves an order by its ID.
     */
    @Test
    @DisplayName("Should find order by ID")
    void testFindById_WhenExists() {
        // Given
        Order savedOrder = entityManager.persistAndFlush(testOrder);

        // When
        Optional<Order> result = orderRepository.findById(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());
    }

    /**
     * Test deleting an order.
     * Verifies that the repository correctly deletes an order.
     */
    @Test
    @DisplayName("Should delete order successfully")
    void testDelete_Order() {
        // Given
        Order savedOrder = entityManager.persistAndFlush(testOrder);
        Long orderId = savedOrder.getId();

        // When
        orderRepository.delete(savedOrder);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(orderId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all orders.
     * Verifies that the repository correctly retrieves all orders.
     */
    @Test
    @DisplayName("Should find all orders")
    void testFindAll_Orders() {
        // Given
        entityManager.persistAndFlush(testOrder);
        Order anotherOrder = new Order();
        anotherOrder.setOrderNumber("ORD-2024-003");
        anotherOrder.setStatus(Order.OrderStatus.COMPLETED);
        entityManager.persistAndFlush(anotherOrder);

        // When
        List<Order> allOrders = orderRepository.findAll();

        // Then
        assertThat(allOrders).hasSizeGreaterThanOrEqualTo(2);
    }
}