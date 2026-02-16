package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for OrderItemRepository.
 * Tests repository methods for OrderItem entity operations.
 * Uses in-memory database for testing.
 *
 * @author QA Automation Team
 * @version 1.0
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
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testOrderItem = new OrderItem();
    }

    /**
     * Test saving an order item.
     * Verifies that the order item is persisted correctly.
     */
    @Test
    @DisplayName("Should save order item successfully")
    void testSave_ValidOrderItem_SavesSuccessfully() {
        // Given
        OrderItem newOrderItem = new OrderItem();

        // When
        OrderItem savedOrderItem = orderItemRepository.save(newOrderItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getId()).isNotNull();
    }

    /**
     * Test finding an order item by ID when it exists.
     * Verifies that the correct order item is returned.
     */
    @Test
    @DisplayName("Should find order item by ID when exists")
    void testFindById_WhenExists_ReturnsOrderItem() {
        // Given
        OrderItem orderItem = new OrderItem();
        OrderItem savedOrderItem = entityManager.persistAndFlush(orderItem);

        // When
        Optional<OrderItem> result = orderItemRepository.findById(savedOrderItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedOrderItem.getId());
    }

    /**
     * Test finding an order item by ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when order item does not exist")
    void testFindById_WhenNotExists_ReturnsEmpty() {
        // Given
        Long nonExistentId = 99999L;

        // When
        Optional<OrderItem> result = orderItemRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an order item by ID.
     * Verifies that the order item is removed from the database.
     */
    @Test
    @DisplayName("Should delete order item by ID successfully")
    void testDeleteById_ExistingOrderItem_DeletesSuccessfully() {
        // Given
        OrderItem orderItem = new OrderItem();
        OrderItem savedOrderItem = entityManager.persistAndFlush(orderItem);
        Long orderItemId = savedOrderItem.getId();

        // When
        orderItemRepository.deleteById(orderItemId);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(orderItemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all order items.
     * Verifies that all order items can be retrieved.
     */
    @Test
    @DisplayName("Should find all order items")
    void testFindAll_ReturnsAllOrderItems() {
        // Given
        OrderItem orderItem1 = new OrderItem();
        OrderItem orderItem2 = new OrderItem();
        entityManager.persist(orderItem1);
        entityManager.persist(orderItem2);
        entityManager.flush();

        // When
        List<OrderItem> result = orderItemRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting order items.
     * Verifies that the count of order items is correct.
     */
    @Test
    @DisplayName("Should count order items correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = orderItemRepository.count();
        OrderItem orderItem = new OrderItem();
        entityManager.persistAndFlush(orderItem);

        // When
        long newCount = orderItemRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test checking if order item exists by ID.
     * Verifies that existence check works correctly.
     */
    @Test
    @DisplayName("Should check if order item exists by ID")
    void testExistsById_WhenExists_ReturnsTrue() {
        // Given
        OrderItem orderItem = new OrderItem();
        OrderItem savedOrderItem = entityManager.persistAndFlush(orderItem);

        // When
        boolean exists = orderItemRepository.existsById(savedOrderItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order item exists by ID when it doesn't exist.
     * Verifies that existence check returns false for non-existent items.
     */
    @Test
    @DisplayName("Should return false when order item does not exist")
    void testExistsById_WhenNotExists_ReturnsFalse() {
        // Given
        Long nonExistentId = 99999L;

        // When
        boolean exists = orderItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test updating an order item.
     * Verifies that the order item can be updated successfully.
     */
    @Test
    @DisplayName("Should update order item successfully")
    void testUpdate_ExistingOrderItem_UpdatesSuccessfully() {
        // Given
        OrderItem orderItem = new OrderItem();
        OrderItem savedOrderItem = entityManager.persistAndFlush(orderItem);
        Long orderItemId = savedOrderItem.getId();

        // When
        OrderItem updatedOrderItem = orderItemRepository.save(savedOrderItem);

        // Then
        assertThat(updatedOrderItem).isNotNull();
        assertThat(updatedOrderItem.getId()).isEqualTo(orderItemId);
    }

    /**
     * Test deleting all order items.
     * Verifies that all order items are removed from the database.
     */
    @Test
    @DisplayName("Should delete all order items successfully")
    void testDeleteAll_RemovesAllOrderItems() {
        // Given
        OrderItem orderItem1 = new OrderItem();
        OrderItem orderItem2 = new OrderItem();
        entityManager.persist(orderItem1);
        entityManager.persist(orderItem2);
        entityManager.flush();

        // When
        orderItemRepository.deleteAll();
        entityManager.flush();

        // Then
        long count = orderItemRepository.count();
        assertThat(count).isEqualTo(0);
    }
}