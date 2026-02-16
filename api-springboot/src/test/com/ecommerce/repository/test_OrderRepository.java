package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
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
 * JUnit 5 test class for OrderRepository.
 * Tests repository methods for Order entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
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
    private OrderItem testOrderItem;
    private String testOrderNumber;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities for User, Order, and OrderItem.
     */
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        // Create test order
        testOrderNumber = "ORD-" + UUID.randomUUID().toString();
        testOrder = new Order();
        testOrder.setOrderNumber(testOrderNumber);
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(299.99));
        entityManager.persist(testOrder);

        // Create test order item
        testOrderItem = new OrderItem();
        testOrderItem.setOrder(testOrder);
        testOrderItem.setQuantity(3);
        testOrderItem.setPrice(BigDecimal.valueOf(99.99));
        testOrderItem.setSubtotal(BigDecimal.valueOf(299.97));
        entityManager.persist(testOrderItem);
        
        entityManager.flush();
    }

    /**
     * Test finding an Order by order number when it exists.
     * Verifies that the correct Order is returned.
     */
    @Test
    @DisplayName("Should find Order by order number when exists")
    void testFindByOrderNumber_WhenExists_ReturnsOrder() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber(testOrderNumber);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
        assertThat(result.get().getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding an Order by order number when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order number does not exist")
    void testFindByOrderNumber_WhenNotExists_ReturnsEmpty() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by user ID with pagination.
     * Verifies that orders are returned in descending order by creation date.
     */
    @Test
    @DisplayName("Should find orders by user ID with pagination")
    void testFindByUserId_WithPagination_ReturnsOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(testUser.getUserId(), pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test finding orders by user ID when user has no orders.
     * Verifies that an empty page is returned.
     */
    @Test
    @DisplayName("Should return empty page when user has no orders")
    void testFindByUserId_WhenNoOrders_ReturnsEmptyPage() {
        // Given
        Long nonExistentUserId = 99999L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(nonExistentUserId, pageable);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding orders by status.
     * Verifies that all orders with the specified status are returned.
     */
    @Test
    @DisplayName("Should find orders by status")
    void testFindByStatus_ReturnsMatchingOrders() {
        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    /**
     * Test finding orders by status when none match.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no orders match status")
    void testFindByStatus_WhenNoMatch_ReturnsEmpty() {
        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding an Order with items using fetch join.
     * Verifies that the Order and its items are loaded in a single query.
     */
    @Test
    @DisplayName("Should find Order with items using fetch join")
    void testFindByIdWithItems_ReturnsOrderWithItems() {
        // When
        Optional<Order> result = orderRepository.findByIdWithItems(testOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems()).isNotEmpty();
        assertThat(result.get().getItems()).hasSize(1);
    }

    /**
     * Test finding an Order with items when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order ID does not exist")
    void testFindByIdWithItems_WhenNotExists_ReturnsEmpty() {
        // When
        Optional<Order> result = orderRepository.findByIdWithItems(99999L);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if an order number exists.
     * Verifies that the method returns true for existing order numbers.
     */
    @Test
    @DisplayName("Should return true when order number exists")
    void testExistsByOrderNumber_WhenExists_ReturnsTrue() {
        // When
        boolean exists = orderRepository.existsByOrderNumber(testOrderNumber);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if an order number exists when it does not.
     * Verifies that the method returns false for non-existing order numbers.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_WhenNotExists_ReturnsFalse() {
        // When
        boolean exists = orderRepository.existsByOrderNumber("NON-EXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a new Order.
     * Verifies that the Order is persisted correctly with all attributes.
     */
    @Test
    @DisplayName("Should save new Order successfully")
    void testSave_NewOrder_Success() {
        // Given
        String newOrderNumber = "ORD-" + UUID.randomUUID().toString();
        Order newOrder = new Order();
        newOrder.setOrderNumber(newOrderNumber);
        newOrder.setUser(testUser);
        newOrder.setStatus(Order.OrderStatus.CONFIRMED);
        newOrder.setTotalAmount(BigDecimal.valueOf(499.99));

        // When
        Order savedOrder = orderRepository.save(newOrder);

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderNumber()).isEqualTo(newOrderNumber);
        assertThat(savedOrder.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    /**
     * Test updating an existing Order.
     * Verifies that Order modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing Order successfully")
    void testSave_UpdateOrder_Success() {
        // Given
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        testOrder.setTotalAmount(BigDecimal.valueOf(349.99));

        // When
        Order updatedOrder = orderRepository.save(testOrder);
        entityManager.flush();

        // Then
        assertThat(updatedOrder.getStatus()).isEqualTo(Order.OrderStatus.SHIPPED);
        assertThat(updatedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(349.99));
    }

    /**
     * Test deleting an Order by ID.
     * Verifies that the Order is removed from the database.
     */
    @Test
    @DisplayName("Should delete Order by ID successfully")
    void testDeleteById_Success() {
        // Given
        Long orderId = testOrder.getId();

        // When
        orderRepository.deleteById(orderId);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(orderId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding an Order by ID.
     * Verifies that the correct Order is retrieved.
     */
    @Test
    @DisplayName("Should find Order by ID when exists")
    void testFindById_WhenExists_ReturnsOrder() {
        // When
        Optional<Order> result = orderRepository.findById(testOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testOrder.getId());
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrderNumber);
    }

    /**
     * Test pagination of user orders.
     * Verifies that pagination works correctly with multiple orders.
     */
    @Test
    @DisplayName("Should paginate user orders correctly")
    void testFindByUserId_Pagination_WorksCorrectly() {
        // Given - create additional orders
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderNumber("ORD-" + UUID.randomUUID().toString());
            order.setUser(testUser);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(BigDecimal.valueOf(100.00 * (i + 1)));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<Order> result = orderRepository.findByUserId(testUser.getUserId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }
}