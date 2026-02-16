package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for OrderItemRepository.
 * Tests all public methods inherited from JpaRepository.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("OrderItemRepository Tests")
public class test_OrderItemRepository {

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
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(BigDecimal.valueOf(99.99));
        // Note: Actual entity setup would require Order and Product entities
        // This is a simplified version for demonstration
    }

    /**
     * Test saving an order item.
     */
    @Test
    @DisplayName("Should save order item successfully")
    void testSaveOrderItem() {
        // When
        OrderItem savedItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // Then
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    /**
     * Test finding order item by ID - success case.
     */
    @Test
    @DisplayName("Should find order item by ID")
    void testFindById_Success() {
        // Given
        OrderItem savedItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        Optional<OrderItem> result = orderItemRepository.findById(savedItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedItem.getId());
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding order item by ID - not found case.
     */
    @Test
    @DisplayName("Should return empty when order item not found by ID")
    void testFindById_NotFound() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<OrderItem> result = orderItemRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all order items.
     */
    @Test
    @DisplayName("Should find all order items")
    void testFindAll() {
        // Given
        orderItemRepository.save(testOrderItem);
        
        OrderItem anotherItem = new OrderItem();
        anotherItem.setQuantity(1);
        anotherItem.setPrice(BigDecimal.valueOf(49.99));
        orderItemRepository.save(anotherItem);
        entityManager.flush();

        // When
        List<OrderItem> items = orderItemRepository.findAll();

        // Then
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test deleting an order item by ID.
     */
    @Test
    @DisplayName("Should delete order item by ID successfully")
    void testDeleteById() {
        // Given
        OrderItem savedItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();
        Long itemId = savedItem.getId();

        // When
        orderItemRepository.deleteById(itemId);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an order item entity.
     */
    @Test
    @DisplayName("Should delete order item entity successfully")
    void testDelete() {
        // Given
        OrderItem savedItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();
        Long itemId = savedItem.getId();

        // When
        orderItemRepository.delete(savedItem);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order item exists by ID.
     */
    @Test
    @DisplayName("Should return true when order item exists")
    void testExistsById_True() {
        // Given
        OrderItem savedItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        boolean exists = orderItemRepository.existsById(savedItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order item exists by ID - not found case.
     */
    @Test
    @DisplayName("Should return false when order item does not exist")
    void testExistsById_False() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = orderItemRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting order items.
     */
    @Test
    @DisplayName("Should count order items correctly")
    void testCount() {
        // Given
        orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        long count = orderItemRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Test updating an order item.
     */
    @Test
    @DisplayName("Should update order item successfully")
    void testUpdateOrderItem() {
        // Given
        OrderItem savedItem = orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        savedItem.setQuantity(5);
        OrderItem updatedItem = orderItemRepository.save(savedItem);
        entityManager.flush();

        // Then
        assertThat(updatedItem.getQuantity()).isEqualTo(5);
    }

    /**
     * Test deleting all order items.
     */
    @Test
    @DisplayName("Should delete all order items")
    void testDeleteAll() {
        // Given
        orderItemRepository.save(testOrderItem);
        entityManager.flush();

        // When
        orderItemRepository.deleteAll();
        entityManager.flush();

        // Then
        long count = orderItemRepository.count();
        assertThat(count).isZero();
    }
}