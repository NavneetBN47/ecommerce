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
 * Tests repository methods for ProductInventory entity operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("ProductInventoryRepository Tests")
public class test_ProductInventoryRepository {

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID testProductId;
    private ProductInventory testInventory;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        testInventory = new ProductInventory();
        testInventory.setId(UUID.randomUUID());
        testInventory.setQuantity(100);
    }

    /**
     * Test finding product inventory by product ID when it exists.
     * Verifies that the repository correctly retrieves inventory for a product.
     */
    @Test
    @DisplayName("Should find product inventory by product ID when exists")
    void testFindByProductId_WhenExists() {
        // Given
        ProductInventory savedInventory = entityManager.persistAndFlush(testInventory);

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isPresent();
    }

    /**
     * Test finding product inventory by product ID when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent inventory.
     */
    @Test
    @DisplayName("Should return empty when product inventory does not exist")
    void testFindByProductId_WhenNotExists() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving product inventory.
     * Verifies that the repository correctly persists product inventory.
     */
    @Test
    @DisplayName("Should save product inventory successfully")
    void testSave_ProductInventory() {
        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
        assertThat(savedInventory.getQuantity()).isEqualTo(100);
    }

    /**
     * Test finding product inventory by ID.
     * Verifies that the repository correctly retrieves inventory by its ID.
     */
    @Test
    @DisplayName("Should find product inventory by ID")
    void testFindById_WhenExists() {
        // Given
        ProductInventory savedInventory = entityManager.persistAndFlush(testInventory);

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(savedInventory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedInventory.getId());
    }

    /**
     * Test updating product inventory quantity.
     * Verifies that the repository correctly updates inventory quantity.
     */
    @Test
    @DisplayName("Should update product inventory quantity")
    void testUpdate_InventoryQuantity() {
        // Given
        ProductInventory savedInventory = entityManager.persistAndFlush(testInventory);
        savedInventory.setQuantity(150);

        // When
        ProductInventory updatedInventory = productInventoryRepository.save(savedInventory);

        // Then
        assertThat(updatedInventory.getQuantity()).isEqualTo(150);
    }

    /**
     * Test deleting product inventory.
     * Verifies that the repository correctly deletes product inventory.
     */
    @Test
    @DisplayName("Should delete product inventory successfully")
    void testDelete_ProductInventory() {
        // Given
        ProductInventory savedInventory = entityManager.persistAndFlush(testInventory);
        UUID inventoryId = savedInventory.getId();

        // When
        productInventoryRepository.delete(savedInventory);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all product inventories.
     * Verifies that the repository correctly retrieves all inventories.
     */
    @Test
    @DisplayName("Should find all product inventories")
    void testFindAll_ProductInventories() {
        // Given
        entityManager.persistAndFlush(testInventory);
        ProductInventory anotherInventory = new ProductInventory();
        anotherInventory.setId(UUID.randomUUID());
        anotherInventory.setQuantity(50);
        entityManager.persistAndFlush(anotherInventory);

        // When
        var allInventories = productInventoryRepository.findAll();

        // Then
        assertThat(allInventories).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test checking if product inventory exists by ID.
     * Verifies that the repository correctly checks inventory existence.
     */
    @Test
    @DisplayName("Should check if product inventory exists by ID")
    void testExistsById() {
        // Given
        ProductInventory savedInventory = entityManager.persistAndFlush(testInventory);

        // When
        boolean exists = productInventoryRepository.existsById(savedInventory.getId());

        // Then
        assertThat(exists).isTrue();
    }
}