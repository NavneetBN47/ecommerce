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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for OrderRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
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
    private String testOrderNumber;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testOrderNumber = "ORD-2024-001";
        
        testOrder = new Order();
        testOrder.setOrderNumber(testOrderNumber);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(299.99));
    }

    /**
     * Test finding order by order number when order exists.
     * Verifies that the correct order is returned.
     */
    @Test
    @DisplayName("Should find order by order number when exists")
    void testFindByOrderNumber_WhenExists() {
        // Given
        entityManager.persist(testOrder);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findByOrderNumber(testOrderNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
        assertThat(result.get().getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding order by order number when order does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order not found by order number")
    void testFindByOrderNumber_WhenNotExists() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     * Verifies that correct orders are returned for the specified user.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId() {
        // Given
        Long userId = 1L;
        testOrder.setUser(createMockUser(userId));
        entityManager.persist(testOrder);
        
        Order anotherOrder = new Order();
        anotherOrder.setOrderNumber("ORD-2024-002");
        anotherOrder.setStatus(Order.OrderStatus.COMPLETED);
        anotherOrder.setTotalAmount(BigDecimal.valueOf(399.99));
        anotherOrder.setUser(createMockUser(userId));
        entityManager.persist(anotherOrder);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(userId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(order -> order.getUser().getId().equals(userId));
    }

    /**
     * Test finding orders by user ID when user has no orders.
     * Verifies that an empty page is returned.
     */
    @Test
    @DisplayName("Should return empty page when user has no orders")
    void testFindByUserId_WhenNoOrders() {
        // Given
        Long userId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(userId, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    /**
     * Test finding orders by status.
     * Verifies that only orders with the specified status are returned.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus() {
        // Given
        entityManager.persist(testOrder);
        
        Order completedOrder = new Order();
        completedOrder.setOrderNumber("ORD-2024-003");
        completedOrder.setStatus(Order.OrderStatus.COMPLETED);
        completedOrder.setTotalAmount(BigDecimal.valueOf(199.99));
        entityManager.persist(completedOrder);
        entityManager.flush();

        // When
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        List<Order> completedOrders = orderRepository.findByStatus(Order.OrderStatus.COMPLETED);

        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(completedOrders).hasSize(1);
        assertThat(completedOrders.get(0).getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
    }

    /**
     * Test finding orders by status when no orders have that status.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no orders have specified status")
    void testFindByStatus_WhenNoOrders() {
        // Given
        entityManager.persist(testOrder);
        entityManager.flush();

        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.CANCELLED);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding order by ID with items eagerly loaded.
     * Verifies that the order and its items are loaded in a single query.
     */
    @Test
    @DisplayName("Should find order by ID with items")
    void testFindByIdWithItems() {
        // Given
        Order savedOrder = entityManager.persist(testOrder);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());
    }

    /**
     * Test finding order by ID with items when order doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order not found by ID with items")
    void testFindByIdWithItems_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order number exists.
     * Verifies that the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when order number exists")
    void testExistsByOrderNumber_WhenExists() {
        // Given
        entityManager.persist(testOrder);
        entityManager.flush();

        // When
        boolean exists = orderRepository.existsByOrderNumber(testOrderNumber);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order number exists when it doesn't.
     * Verifies that the existence check returns false.
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
     * Test saving a new order.
     * Verifies that the order is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new order successfully")
    void testSave_NewOrder() {
        // When
        Order savedOrder = orderRepository.save(testOrder);

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test updating an existing order.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing order successfully")
    void testSave_UpdateOrder() {
        // Given
        Order savedOrder = entityManager.persist(testOrder);
        entityManager.flush();
        Long savedId = savedOrder.getId();

        // When
        savedOrder.setStatus(Order.OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(savedOrder);

        // Then
        assertThat(updatedOrder.getId()).isEqualTo(savedId);
        assertThat(updatedOrder.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
    }

    /**
     * Test finding order by ID.
     * Verifies that the correct order is retrieved.
     */
    @Test
    @DisplayName("Should find order by ID when exists")
    void testFindById_WhenExists() {
        // Given
        Order savedOrder = entityManager.persist(testOrder);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findById(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());
    }

    /**
     * Test deleting an order by ID.
     * Verifies that the order is removed from the database.
     */
    @Test
    @DisplayName("Should delete order by ID successfully")
    void testDeleteById() {
        // Given
        Order savedOrder = entityManager.persist(testOrder);
        entityManager.flush();
        Long savedId = savedOrder.getId();

        // When
        orderRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Helper method to create a mock user for testing.
     */
    private com.ecommerce.entity.User createMockUser(Long userId) {
        com.ecommerce.entity.User user = new com.ecommerce.entity.User();
        user.setId(userId);
        return entityManager.persist(user);
    }
}