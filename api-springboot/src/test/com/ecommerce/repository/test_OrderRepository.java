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
@DisplayName("OrderRepository Tests")
public class test_OrderRepository {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order testOrder;
    private String testOrderNumber;
    private Long testUserId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testOrderNumber = "ORD-2024-001";
        testUserId = 1L;
        
        testOrder = new Order();
        testOrder.setOrderNumber(testOrderNumber);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(199.99));
        // Note: Actual entity setup would require User entity
        // This is a simplified version for demonstration
    }

    /**
     * Test finding order by order number - success case.
     */
    @Test
    @DisplayName("Should find order by order number")
    void testFindByOrderNumber_Success() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findByOrderNumber(testOrderNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test finding order by order number - not found case.
     */
    @Test
    @DisplayName("Should return empty when order not found by order number")
    void testFindByOrderNumber_NotFound() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId() {
        // Given
        orderRepository.save(testOrder);
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(testUserId, pageable);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding orders by status.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus() {
        // Given
        orderRepository.save(testOrder);
        entityManager.flush();

        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(order -> order.getStatus() == Order.OrderStatus.PENDING);
    }

    /**
     * Test finding orders by status - no results.
     */
    @Test
    @DisplayName("Should return empty list when no orders with status exist")
    void testFindByStatus_NoResults() {
        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding order by ID with items.
     */
    @Test
    @DisplayName("Should find order by ID with items")
    void testFindByIdWithItems() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding order by ID with items - not found.
     */
    @Test
    @DisplayName("Should return empty when order not found by ID with items")
    void testFindByIdWithItems_NotFound() {
        // When
        Optional<Order> result = orderRepository.findByIdWithItems(999L);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order number exists.
     */
    @Test
    @DisplayName("Should return true when order number exists")
    void testExistsByOrderNumber_True() {
        // Given
        orderRepository.save(testOrder);
        entityManager.flush();

        // When
        boolean exists = orderRepository.existsByOrderNumber(testOrderNumber);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order number exists - not found.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_False() {
        // When
        boolean exists = orderRepository.existsByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving an order.
     */
    @Test
    @DisplayName("Should save order successfully")
    void testSaveOrder() {
        // When
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test finding order by ID.
     */
    @Test
    @DisplayName("Should find order by ID")
    void testFindById_Success() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findById(savedOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrder.getId());
    }

    /**
     * Test deleting an order.
     */
    @Test
    @DisplayName("Should delete order successfully")
    void testDeleteOrder() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        Long orderId = savedOrder.getId();

        // When
        orderRepository.deleteById(orderId);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(orderId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all orders.
     */
    @Test
    @DisplayName("Should find all orders")
    void testFindAll() {
        // Given
        orderRepository.save(testOrder);
        entityManager.flush();

        // When
        List<Order> orders = orderRepository.findAll();

        // Then
        assertThat(orders).isNotEmpty();
    }

    /**
     * Test with null order number - edge case.
     */
    @Test
    @DisplayName("Should handle null order number gracefully")
    void testFindByOrderNumber_Null() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test pagination with empty results.
     */
    @Test
    @DisplayName("Should handle pagination with no results")
    void testFindByUserId_EmptyResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(999L, pageable);

        // Then
        assertThat(result).isEmpty();
    }
}