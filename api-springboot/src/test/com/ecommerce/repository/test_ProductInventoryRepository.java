package com.ecommerce.repository;

import com.ecommerce.entity.ProductInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductInventoryRepository.
 * Tests repository operations for ProductInventory entity including custom query methods.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductInventoryRepository Tests")
class test_ProductInventoryRepository {

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testProductId;
    private ProductInventory testInventory;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        
        testInventory = new ProductInventory();
        testInventory.setId(UUID.randomUUID());
    }

    /**
     * Test finding product inventory by product ID when it exists.
     * Verifies that the custom query method returns the correct inventory.
     */
    @Test
    @DisplayName("Should find product inventory by product ID when exists")
    void testFindByProductId_WhenExists_ShouldReturnInventory() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding product inventory by product ID when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when product inventory does not exist")
    void testFindByProductId_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding product inventory with null product ID.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null product ID gracefully")
    void testFindByProductId_WithNullProductId_ShouldReturnEmpty() {
        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving product inventory.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save product inventory successfully")
    void testSave_ShouldPersistInventory() {
        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // Then
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
    }

    /**
     * Test finding product inventory by ID.
     * Verifies that findById returns the correct inventory.
     */
    @Test
    @DisplayName("Should find product inventory by ID when exists")
    void testFindById_WhenExists_ShouldReturnInventory() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();
        UUID savedId = savedInventory.getId();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
    }

    /**
     * Test finding product inventory by ID when it does not exist.
     * Verifies that findById returns empty Optional.
     */
    @Test
    @DisplayName("Should return empty when inventory ID does not exist")
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting product inventory.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete product inventory successfully")
    void testDelete_ShouldRemoveInventory() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();
        UUID savedId = savedInventory.getId();

        // When
        productInventoryRepository.delete(savedInventory);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all product inventories.
     * Verifies that the count operation returns correct number.
     */
    @Test
    @DisplayName("Should count all product inventories correctly")
    void testCount_ShouldReturnCorrectCount() {
        // Given
        productInventoryRepository.save(testInventory);
        entityManager.flush();

        // When
        long count = productInventoryRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /**
     * Test checking if product inventory exists by ID.
     * Verifies that existsById returns true for existing inventory.
     */
    @Test
    @DisplayName("Should return true when product inventory exists")
    void testExistsById_WhenExists_ShouldReturnTrue() {
        // Given
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();
        UUID savedId = savedInventory.getId();

        // When
        boolean exists = productInventoryRepository.existsById(savedId);

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if product inventory exists when it does not.
     * Verifies that existsById returns false.
     */
    @Test
    @DisplayName("Should return false when product inventory does not exist")
    void testExistsById_WhenNotExists_ShouldReturnFalse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productInventoryRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }
}