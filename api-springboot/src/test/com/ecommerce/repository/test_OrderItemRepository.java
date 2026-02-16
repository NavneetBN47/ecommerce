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
 * Tests repository methods for OrderItem entity operations.
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
    }

    /**
     * Test saving an order item.
     * Verifies that the repository correctly persists an order item.
     */
    @Test
    @DisplayName("Should save order item successfully")
    void testSave_OrderItem() {
        // When
        OrderItem savedItem = orderItemRepository.save(testOrderItem);

        // Then
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    /**
     * Test finding an order item by ID when it exists.
     * Verifies that the repository correctly retrieves an order item by its ID.
     */
    @Test
    @DisplayName("Should find order item by ID when exists")
    void testFindById_WhenExists() {
        // Given
        OrderItem savedItem = entityManager.persistAndFlush(testOrderItem);

        // When
        Optional<OrderItem> result = orderItemRepository.findById(savedItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedItem.getId());
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    /**
     * Test finding an order item by ID when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent item.
     */
    @Test
    @DisplayName("Should return empty when order item does not exist")
    void testFindById_WhenNotExists() {
        // When
        Optional<OrderItem> result = orderItemRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all order items.
     * Verifies that the repository correctly retrieves all order items.
     */
    @Test
    @DisplayName("Should find all order items")
    void testFindAll_OrderItems() {
        // Given
        entityManager.persistAndFlush(testOrderItem);
        OrderItem anotherItem = new OrderItem();
        anotherItem.setQuantity(1);
        anotherItem.setPrice(BigDecimal.valueOf(49.99));
        entityManager.persistAndFlush(anotherItem);

        // When
        List<OrderItem> allItems = orderItemRepository.findAll();

        // Then
        assertThat(allItems).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test deleting an order item.
     * Verifies that the repository correctly deletes an order item.
     */
    @Test
    @DisplayName("Should delete order item successfully")
    void testDelete_OrderItem() {
        // Given
        OrderItem savedItem = entityManager.persistAndFlush(testOrderItem);
        Long itemId = savedItem.getId();

        // When
        orderItemRepository.delete(savedItem);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting an order item by ID.
     * Verifies that the repository correctly deletes an order item by its ID.
     */
    @Test
    @DisplayName("Should delete order item by ID")
    void testDeleteById_OrderItem() {
        // Given
        OrderItem savedItem = entityManager.persistAndFlush(testOrderItem);
        Long itemId = savedItem.getId();

        // When
        orderItemRepository.deleteById(itemId);
        entityManager.flush();

        // Then
        Optional<OrderItem> result = orderItemRepository.findById(itemId);
        assertThat(result).isEmpty();
    }

    /**
     * Test checking if order item exists by ID.
     * Verifies that the repository correctly checks order item existence.
     */
    @Test
    @DisplayName("Should check if order item exists by ID")
    void testExistsById() {
        // Given
        OrderItem savedItem = entityManager.persistAndFlush(testOrderItem);

        // When
        boolean exists = orderItemRepository.existsById(savedItem.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if order item does not exist.
     * Verifies that the repository correctly returns false for non-existent item.
     */
    @Test
    @DisplayName("Should return false when order item does not exist")
    void testExistsById_WhenNotExists() {
        // When
        boolean exists = orderItemRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all order items.
     * Verifies that the repository correctly counts order items.
     */
    @Test
    @DisplayName("Should count all order items")
    void testCount_OrderItems() {
        // Given
        entityManager.persistAndFlush(testOrderItem);
        OrderItem anotherItem = new OrderItem();
        anotherItem.setQuantity(3);
        anotherItem.setPrice(BigDecimal.valueOf(29.99));
        entityManager.persistAndFlush(anotherItem);

        // When
        long count = orderItemRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}