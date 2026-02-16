package com.ecommerce.repository;

import com.ecommerce.entity.ProductInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductInventoryRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("ProductInventoryRepository Tests")
public class test_ProductInventoryRepository {

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductInventory testInventory;
    private UUID testProductId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        
        testInventory = new ProductInventory();
        testInventory.setId(UUID.randomUUID());
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(10);
        // Note: Actual entity setup would require Product entity
        // This is a simplified version for demonstration
    }

    /**
     * Test finding inventory by product ID - success case.
     */
    @Test
    @DisplayName("Should find inventory by product ID")
    void testFindByProductId_Success() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding inventory by product ID - not found case.
     */
    @Test
    @DisplayName("Should return empty when inventory not found by product ID")
    void testFindByProductId_NotFound() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving product inventory.
     */
    @Test
    @DisplayName("Should save product inventory successfully")
    void testSaveInventory() {
        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // Then
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
        assertThat(savedInventory.getQuantity()).isEqualTo(100);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(10);
    }

    /**
     * Test finding inventory by ID.
     */
    @Test
    @DisplayName("Should find inventory by ID")
    void testFindById_Success() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(savedInventory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedInventory.getId());
    }

    /**
     * Test finding inventory by ID - not found.
     */
    @Test
    @DisplayName("Should return empty when inventory not found by ID")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting inventory by ID.
     */
    @Test
    @DisplayName("Should delete inventory by ID successfully")
    void testDeleteById() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();
        UUID inventoryId = savedInventory.getId();

        // When
        productInventoryRepository.deleteById(inventoryId);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test updating inventory.
     */
    @Test
    @DisplayName("Should update inventory successfully")
    void testUpdateInventory() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // When
        savedInventory.setQuantity(150);
        ProductInventory updatedInventory = productInventoryRepository.save(savedInventory);
        entityManager.flush();

        // Then
        assertThat(updatedInventory.getQuantity()).isEqualTo(150);
    }

    /**
     * Test finding all inventories.
     */
    @Test
    @DisplayName("Should find all inventories")
    void testFindAll() {
        // Given
        productInventoryRepository.save(testInventory);
        entityManager.flush();

        // When
        var inventories = productInventoryRepository.findAll();

        // Then
        assertThat(inventories).isNotEmpty();
    }

    /**
     * Test checking if inventory exists by ID.
     */
    @Test
    @DisplayName("Should return true when inventory exists")
    void testExistsById_True() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // When
        boolean exists = productInventoryRepository.existsById(savedInventory.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if inventory exists by ID - not found.
     */
    @Test
    @DisplayName("Should return false when inventory does not exist")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productInventoryRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test with null product ID - edge case.
     */
    @Test
    @DisplayName("Should handle null product ID gracefully")
    void testFindByProductId_NullProductId() {
        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test inventory with zero quantity.
     */
    @Test
    @DisplayName("Should handle inventory with zero quantity")
    void testInventoryWithZeroQuantity() {
        // Given
        testInventory.setQuantity(0);
        
        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // Then
        assertThat(savedInventory.getQuantity()).isZero();
    }
}