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
 * Tests repository operations for Order entity including custom query methods.
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
    private String testOrderNumber;
    private Long testUserId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testOrderNumber = "ORD-12345";
        testUserId = 1L;
        
        testOrder = new Order();
        testOrder.setOrderNumber(testOrderNumber);
        testOrder.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding an order by order number when it exists.
     * Verifies that the custom query method returns the correct order.
     */
    @Test
    @DisplayName("Should find order by order number when exists")
    void testFindByOrderNumber_WhenExists_ShouldReturnOrder() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Order> result = orderRepository.findByOrderNumber(testOrderNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test finding an order by order number when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when order number does not exist")
    void testFindByOrderNumber_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     * Verifies that the method returns paginated results.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId_ShouldReturnPagedOrders() {
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
     * Test finding orders by user ID when user has no orders.
     * Verifies that an empty page is returned.
     */
    @Test
    @DisplayName("Should return empty page when user has no orders")
    void testFindByUserId_WhenNoOrders_ShouldReturnEmptyPage() {
        // Given
        Long nonExistentUserId = 999999L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(nonExistentUserId, pageable);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by status.
     * Verifies that only orders with the specified status are returned.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus_ShouldReturnOrdersWithStatus() {
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
     * Test finding orders by status when no orders match.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no orders match status")
    void testFindByStatus_WhenNoMatch_ShouldReturnEmptyList() {
        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.CANCELLED);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding an order with items by ID.
     * Verifies that the order and its items are fetched together.
     */
    @Test
    @DisplayName("Should find order with items by ID")
    void testFindByIdWithItems_WhenExists_ShouldReturnOrderWithItems() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        Long savedId = savedOrder.getId();

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test finding an order with items when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty when order with items does not exist")
    void testFindByIdWithItems_WhenNotExists_ShouldReturnEmpty() {
        // Given
        Long nonExistentId = 999999L;

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if an order number exists.
     * Verifies that existsByOrderNumber returns true for existing order.
     */
    @Test
    @DisplayName("Should return true when order number exists")
    void testExistsByOrderNumber_WhenExists_ShouldReturnTrue() {
        // Given
        orderRepository.save(testOrder);
        entityManager.flush();

        // When
        boolean exists = orderRepository.existsByOrderNumber(testOrderNumber);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if an order number exists when it does not.
     * Verifies that existsByOrderNumber returns false.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_WhenNotExists_ShouldReturnFalse() {
        // When
        boolean exists = orderRepository.existsByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving an order.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save order successfully")
    void testSave_ShouldPersistOrder() {
        // When
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
    }

    /**
     * Test deleting an order.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete order successfully")
    void testDelete_ShouldRemoveOrder() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        entityManager.flush();
        Long savedId = savedOrder.getId();

        // When
        orderRepository.delete(savedOrder);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(savedId);
        assertThat(result).isEmpty();
    }
}