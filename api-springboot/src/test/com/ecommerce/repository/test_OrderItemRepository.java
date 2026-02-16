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
 * Tests repository operations for OrderItem entity.
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
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testOrderItem = new OrderItem();
        // Set up basic order item properties
    }

    /**
     * Test saving an order item.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save order item successfully")
    void testSave_ShouldPersistOrderItem() {
        // When
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getId()).isNotNull();
    }

    /**
     * Test finding an order item by ID when it exists.
     * Verifies that findById returns the correct order item.
     */
    @Test
    @DisplayName("Should find order item by ID when exists")
    void testFindById_WhenExists_ShouldReturnOrderItem() {
        // Given
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();
        Long savedId = savedOrderItem.getId();

        // When
        Optional<OrderItem> result = orderItemRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test finding an order item by ID when it does not exist.
     * Verifies that findById returns empty Optional.
     */
    @Test
    @DisplayName("Should return empty when order item ID does not exist")
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        Long nonExistentId = 999999L;

        // When
        Optional<OrderItem> result = orderItemRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all order items.
     * Verifies that findAll returns all persisted order items.
     */
    @Test
    @DisplayName("Should find all order items")
    void testFindAll_ShouldReturnAllOrderItems() {
        // Given
        orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        List<OrderItem> result = orderItemRepository.findAll();

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test deleting an order item.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete order item successfully")
    void testDelete_ShouldRemoveOrderItem() {
        // Given
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);
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
     * Test deleting an order item by ID.
     * Verifies that deleteById works correctly.
     */
    @Test
    @DisplayName("Should delete order item by ID successfully")
    void testDeleteById_ShouldRemoveOrderItem() {
        // Given
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);
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
     * Test counting all order items.
     * Verifies that the count operation returns correct number.
     */
    @Test
    @DisplayName("Should count all order items correctly")
    void testCount_ShouldReturnCorrectCount() {
        // Given
        orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        long count = orderItemRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Test checking if an order item exists by ID.
     * Verifies that existsById returns true for existing order item.
     */
    @Test
    @DisplayName("Should return true when order item exists")
    void testExistsById_WhenExists_ShouldReturnTrue() {
        // Given
        OrderItem savedOrderItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();
        Long savedId = savedOrderItem.getId();

        // When
        boolean exists = orderItemRepository.existsById(savedId);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if an order item exists by ID when it does not exist.
     * Verifies that existsById returns false.
     */
    @Test
    @DisplayName("Should return false when order item does not exist")
    void testExistsById_WhenNotExists_ShouldReturnFalse() {
        // Given
        Long nonExistentId = 999999L;

        // When
        boolean exists = orderItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}