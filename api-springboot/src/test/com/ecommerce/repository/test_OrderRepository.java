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
 * Tests repository methods for Order entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Order Repository Tests")
class test_OrderRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Order testOrder;
    private Product testProduct;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities in the in-memory database.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        entityManager.persist(testUser);

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);

        testOrder = new Order();
        testOrder.setOrderNumber("ORD-" + System.currentTimeMillis());
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(199.99));
        entityManager.persist(testOrder);

        entityManager.flush();
    }

    /**
     * Test finding an Order by order number.
     * Verifies that the custom query method returns the correct Order.
     */
    @Test
    @DisplayName("Should find Order by order number")
    void testFindByOrderNumber_Success() {
        // When
        Optional<Order> result = orderRepository.findByOrderNumber(testOrder.getOrderNumber());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(testOrder.getOrderNumber());
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding an Order with non-existent order number.
     * Verifies that the method returns empty Optional when order doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when order number does not exist")
    void testFindByOrderNumber_NotFound() {
        // Given
        String nonExistentOrderNumber = "ORD-NONEXISTENT";

        // When
        Optional<Order> result = orderRepository.findByOrderNumber(nonExistentOrderNumber);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding Orders by user ID with pagination.
     * Verifies that the custom query method returns paginated orders for a user.
     */
    @Test
    @DisplayName("Should find Orders by user ID with pagination")
    void testFindByUserId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(testUser.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Test finding Orders by user ID with no results.
     * Verifies that an empty page is returned when user has no orders.
     */
    @Test
    @DisplayName("Should return empty page when user has no orders")
    void testFindByUserId_NoOrders() {
        // Given
        Long nonExistentUserId = 99999L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> result = orderRepository.findByUserId(nonExistentUserId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    /**
     * Test finding Orders by status.
     * Verifies that the method returns all orders with the specified status.
     */
    @Test
    @DisplayName("Should find Orders by status")
    void testFindByStatus_Success() {
        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.PENDING);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(order -> order.getStatus() == Order.OrderStatus.PENDING);
    }

    /**
     * Test finding Orders by status with no results.
     * Verifies that an empty list is returned when no orders have the specified status.
     */
    @Test
    @DisplayName("Should return empty list when no orders have the specified status")
    void testFindByStatus_NoResults() {
        // When
        List<Order> result = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding Order with items using fetch join.
     * Verifies that the custom query method returns order with eagerly loaded items.
     */
    @Test
    @DisplayName("Should find Order with items using fetch join")
    void testFindByIdWithItems_Success() {
        // Given
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        orderItem.setPrice(BigDecimal.valueOf(99.99));
        orderItem.setSubtotal(BigDecimal.valueOf(199.98));
        entityManager.persist(orderItem);
        entityManager.flush();

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(testOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems()).isNotEmpty();
        assertThat(result.get().getItems()).hasSize(1);
    }

    /**
     * Test finding Order with items when order doesn't exist.
     * Verifies that the method returns empty Optional for non-existent order.
     */
    @Test
    @DisplayName("Should return empty Optional when order ID does not exist")
    void testFindByIdWithItems_NotFound() {
        // Given
        Long nonExistentId = 99999L;

        // When
        Optional<Order> result = orderRepository.findByIdWithItems(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order number exists.
     * Verifies that the method returns true when order number exists.
     */
    @Test
    @DisplayName("Should return true when order number exists")
    void testExistsByOrderNumber_True() {
        // When
        boolean exists = orderRepository.existsByOrderNumber(testOrder.getOrderNumber());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order number exists with non-existent number.
     * Verifies that the method returns false when order number doesn't exist.
     */
    @Test
    @DisplayName("Should return false when order number does not exist")
    void testExistsByOrderNumber_False() {
        // Given
        String nonExistentOrderNumber = "ORD-NONEXISTENT";

        // When
        boolean exists = orderRepository.existsByOrderNumber(nonExistentOrderNumber);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving a new Order.
     * Verifies that the repository can persist a new Order entity.
     */
    @Test
    @DisplayName("Should save a new Order successfully")
    void testSave_NewOrder() {
        // Given
        Order newOrder = new Order();
        newOrder.setOrderNumber("ORD-" + System.currentTimeMillis());
        newOrder.setUser(testUser);
        newOrder.setStatus(Order.OrderStatus.PENDING);
        newOrder.setTotalAmount(BigDecimal.valueOf(299.99));

        // When
        Order savedOrder = orderRepository.save(newOrder);

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(299.99));
    }

    /**
     * Test updating an existing Order.
     * Verifies that the repository can update Order properties.
     */
    @Test
    @DisplayName("Should update existing Order successfully")
    void testSave_UpdateOrder() {
        // Given
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);

        // When
        Order updatedOrder = orderRepository.save(testOrder);

        // Then
        assertThat(updatedOrder.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    /**
     * Test finding an Order by ID.
     * Verifies that the repository can retrieve an Order by its primary key.
     */
    @Test
    @DisplayName("Should find Order by ID")
    void testFindById_Success() {
        // When
        Optional<Order> result = orderRepository.findById(testOrder.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testOrder.getId());
    }

    /**
     * Test deleting an Order.
     * Verifies that the repository can delete an Order entity.
     */
    @Test
    @DisplayName("Should delete Order successfully")
    void testDelete_Success() {
        // Given
        Long orderId = testOrder.getId();

        // When
        orderRepository.delete(testOrder);
        entityManager.flush();

        // Then
        Optional<Order> result = orderRepository.findById(orderId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all Orders.
     * Verifies that the repository can count the total number of Orders.
     */
    @Test
    @DisplayName("Should count all Orders")
    void testCount_Success() {
        // When
        long count = orderRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test pagination functionality.
     * Verifies that pagination works correctly for user orders.
     */
    @Test
    @DisplayName("Should paginate user orders correctly")
    void testFindByUserId_Pagination() {
        // Given - Create multiple orders
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderNumber("ORD-" + System.currentTimeMillis() + "-" + i);
            order.setUser(testUser);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(BigDecimal.valueOf(100.00 * (i + 1)));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<Order> result = orderRepository.findByUserId(testUser.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(6);
        assertThat(result.hasNext()).isTrue();
    }

    /**
     * Test finding orders with different statuses.
     * Verifies that orders can be filtered by multiple statuses.
     */
    @Test
    @DisplayName("Should find orders with different statuses")
    void testFindByStatus_MultipleStatuses() {
        // Given
        Order confirmedOrder = new Order();
        confirmedOrder.setOrderNumber("ORD-CONFIRMED");
        confirmedOrder.setUser(testUser);
        confirmedOrder.setStatus(Order.OrderStatus.CONFIRMED);
        confirmedOrder.setTotalAmount(BigDecimal.valueOf(150.00));
        entityManager.persist(confirmedOrder);
        entityManager.flush();

        // When
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        List<Order> confirmedOrders = orderRepository.findByStatus(Order.OrderStatus.CONFIRMED);

        // Then
        assertThat(pendingOrders).isNotEmpty();
        assertThat(confirmedOrders).isNotEmpty();
        assertThat(pendingOrders).allMatch(o -> o.getStatus() == Order.OrderStatus.PENDING);
        assertThat(confirmedOrders).allMatch(o -> o.getStatus() == Order.OrderStatus.CONFIRMED);
    }
}