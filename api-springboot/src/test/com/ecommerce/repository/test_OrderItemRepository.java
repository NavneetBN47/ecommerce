package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for OrderItemRepository.
 * Tests all public methods from JpaRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderItemRepository Tests")
class test_OrderItemRepository {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private OrderItem testOrderItem;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testOrderItem = new OrderItem();
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(BigDecimal.valueOf(99.99));
    }

    /**
     * Test saving a new order item.
     * Verifies that the order item is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new order item successfully")
    void testSave_NewOrderItem() {
        // When
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(2);
        assertThat(savedOrderItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    /**
     * Test updating an existing order item.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing order item successfully")
    void testSave_UpdateOrderItem() {
        // Given
        OrderItem savedOrderItem = entityManager.persist(testOrderItem);
        entityManager.flush();
        Long savedId = savedOrderItem.getId();

        // When
        savedOrderItem.setQuantity(5);
        savedOrderItem.setPrice(BigDecimal.valueOf(149.99));
        OrderItem updatedOrderItem = orderItemRepository.save(savedOrderItem);

        // Then
        assertThat(updatedOrderItem.getId()).isEqualTo(savedId);
        assertThat(updatedOrderItem.getQuantity()).isEqualTo(5);
        assertThat(updatedOrderItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(149.99));
    }

    /**
     * Test finding order item by ID when it exists.
     * Verifies that the correct order item is retrieved.
     */
    @Test
    @DisplayName("Should find order item by ID when exists")
    void testFindById_WhenExists() {
        // Given
        OrderItem savedOrderItem = entityManager.persist(testOrderItem);
        entityManager.flush();

        // When
        Optional<OrderItem> result = orderItemRepository.findById(savedOrderItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrderItem.getId());
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding order item by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order item not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<OrderItem> result = orderItemRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all order items.
     * Verifies that all persisted order items are retrieved.
     */
    @Test
    @DisplayName("Should find all order items")
    void testFindAll() {
        // Given
        entityManager.persist(testOrderItem);
        
        OrderItem anotherOrderItem = new OrderItem();
        anotherOrderItem.setQuantity(3);
        anotherOrderItem.setPrice(BigDecimal.valueOf(199.99));
        entityManager.persist(anotherOrderItem);
        entityManager.flush();

        // When
        List<OrderItem> allOrderItems = orderItemRepository.findAll();

        // Then
        assertThat(allOrderItems).hasSize(2);
    }

    /**
     * Test deleting an order item by ID.
     * Verifies that the order item is removed from the database.
     */
    @Test
    @DisplayName("Should delete order item by ID successfully")
    void testDeleteById() {
        // Given
        OrderItem savedOrderItem = entityManager.persist(testOrderItem);
        entityManager.flush();
        Long savedId = savedOrderItem.getId();

        // When
        orderItemRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an order item entity.
     * Verifies that the order item is removed from the database.
     */
    @Test
    @DisplayName("Should delete order item entity successfully")
    void testDelete() {
        // Given
        OrderItem savedOrderItem = entityManager.persist(testOrderItem);
        entityManager.flush();
        Long savedId = savedOrderItem.getId();

        // When
        orderItemRepository.delete(savedOrderItem);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order item exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when order item exists by ID")
    void testExistsById_WhenExists() {
        // Given
        OrderItem savedOrderItem = entityManager.persist(testOrderItem);
        entityManager.flush();

        // When
        boolean exists = orderItemRepository.existsById(savedOrderItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order item exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when order item does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = orderItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all order items.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all order items correctly")
    void testCount() {
        // Given
        entityManager.persist(testOrderItem);
        
        OrderItem anotherOrderItem = new OrderItem();
        anotherOrderItem.setQuantity(1);
        anotherOrderItem.setPrice(BigDecimal.valueOf(49.99));
        entityManager.persist(anotherOrderItem);
        entityManager.flush();

        // When
        long count = orderItemRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    /**
     * Test deleting all order items.
     * Verifies that all order items are removed from the database.
     */
    @Test
    @DisplayName("Should delete all order items successfully")
    void testDeleteAll() {
        // Given
        entityManager.persist(testOrderItem);
        OrderItem anotherOrderItem = new OrderItem();
        anotherOrderItem.setQuantity(1);
        anotherOrderItem.setPrice(BigDecimal.valueOf(49.99));
        entityManager.persist(anotherOrderItem);
        entityManager.flush();

        // When
        orderItemRepository.deleteAll();
        entityManager.flush();

        // Then
        long count = orderItemRepository.count();
        assertThat(count).isZero();
    }
}