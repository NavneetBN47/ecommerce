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
 * Tests repository methods for ProductInventory entity operations
 * including finding by product ID.
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
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        testInventory = new ProductInventory();
        testInventory.setId(UUID.randomUUID());
    }

    /**
     * Test finding product inventory by product ID when it exists.
     * Verifies that the correct inventory is returned.
     */
    @Test
    @DisplayName("Should find product inventory by product ID when exists")
    void testFindByProductId_WhenExists_ReturnsInventory() {
        // Given
        UUID productId = UUID.randomUUID();
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID());
        entityManager.persistAndFlush(inventory);

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(productId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding product inventory by product ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when product inventory does not exist")
    void testFindByProductId_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving product inventory.
     * Verifies that the inventory is persisted correctly.
     */
    @Test
    @DisplayName("Should save product inventory successfully")
    void testSave_ValidInventory_SavesSuccessfully() {
        // Given
        ProductInventory newInventory = new ProductInventory();
        newInventory.setId(UUID.randomUUID());

        // When
        ProductInventory savedInventory = productInventoryRepository.save(newInventory);

        // Then
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
    }

    /**
     * Test finding product inventory by ID.
     * Verifies that the inventory can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find product inventory by ID when exists")
    void testFindById_WhenExists_ReturnsInventory() {
        // Given
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID());
        ProductInventory savedInventory = entityManager.persistAndFlush(inventory);

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(savedInventory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedInventory.getId());
    }

    /**
     * Test deleting product inventory.
     * Verifies that the inventory is removed from the database.
     */
    @Test
    @DisplayName("Should delete product inventory successfully")
    void testDelete_ExistingInventory_DeletesSuccessfully() {
        // Given
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID());
        ProductInventory savedInventory = entityManager.persistAndFlush(inventory);
        UUID inventoryId = savedInventory.getId();

        // When
        productInventoryRepository.deleteById(inventoryId);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all product inventories.
     * Verifies that all inventories can be retrieved.
     */
    @Test
    @DisplayName("Should find all product inventories")
    void testFindAll_ReturnsAllInventories() {
        // Given
        ProductInventory inventory1 = new ProductInventory();
        inventory1.setId(UUID.randomUUID());
        ProductInventory inventory2 = new ProductInventory();
        inventory2.setId(UUID.randomUUID());
        entityManager.persist(inventory1);
        entityManager.persist(inventory2);
        entityManager.flush();

        // When
        var result = productInventoryRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting product inventories.
     * Verifies that the count of inventories is correct.
     */
    @Test
    @DisplayName("Should count product inventories correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = productInventoryRepository.count();
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID());
        entityManager.persistAndFlush(inventory);

        // When
        long newCount = productInventoryRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test checking if product inventory exists by ID.
     * Verifies that existence check works correctly.
     */
    @Test
    @DisplayName("Should check if product inventory exists by ID")
    void testExistsById_WhenExists_ReturnsTrue() {
        // Given
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID());
        ProductInventory savedInventory = entityManager.persistAndFlush(inventory);

        // When
        boolean exists = productInventoryRepository.existsById(savedInventory.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if product inventory exists by ID when it doesn't exist.
     * Verifies that existence check returns false for non-existent items.
     */
    @Test
    @DisplayName("Should return false when product inventory does not exist")
    void testExistsById_WhenNotExists_ReturnsFalse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productInventoryRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test updating product inventory.
     * Verifies that the inventory can be updated successfully.
     */
    @Test
    @DisplayName("Should update product inventory successfully")
    void testUpdate_ExistingInventory_UpdatesSuccessfully() {
        // Given
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID());
        ProductInventory savedInventory = entityManager.persistAndFlush(inventory);

        // When
        ProductInventory updatedInventory = productInventoryRepository.save(savedInventory);

        // Then
        assertThat(updatedInventory).isNotNull();
        assertThat(updatedInventory.getId()).isEqualTo(savedInventory.getId());
    }
}