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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
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
        testInventory.setProductId(testProductId);
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(10);
    }

    /**
     * Test finding product inventory by product ID when inventory exists.
     * Verifies that the correct inventory is returned.
     */
    @Test
    @DisplayName("Should find product inventory by product ID when exists")
    void testFindByProductId_WhenExists() {
        // Given
        entityManager.persist(testInventory);
        entityManager.flush();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(testProductId);
        assertThat(result.get().getQuantity()).isEqualTo(100);
        assertThat(result.get().getReservedQuantity()).isEqualTo(10);
    }

    /**
     * Test finding product inventory by product ID when inventory does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when inventory not found by product ID")
    void testFindByProductId_WhenNotExists() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new product inventory.
     * Verifies that the inventory is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new product inventory successfully")
    void testSave_NewInventory() {
        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
        assertThat(savedInventory.getProductId()).isEqualTo(testProductId);
        assertThat(savedInventory.getQuantity()).isEqualTo(100);
    }

    /**
     * Test updating an existing product inventory.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing product inventory successfully")
    void testSave_UpdateInventory() {
        // Given
        ProductInventory savedInventory = entityManager.persist(testInventory);
        entityManager.flush();
        UUID savedId = savedInventory.getId();

        // When
        savedInventory.setQuantity(150);
        savedInventory.setReservedQuantity(20);
        ProductInventory updatedInventory = productInventoryRepository.save(savedInventory);

        // Then
        assertThat(updatedInventory.getId()).isEqualTo(savedId);
        assertThat(updatedInventory.getQuantity()).isEqualTo(150);
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(20);
    }

    /**
     * Test finding product inventory by ID.
     * Verifies that the correct inventory is retrieved.
     */
    @Test
    @DisplayName("Should find product inventory by ID when exists")
    void testFindById_WhenExists() {
        // Given
        ProductInventory savedInventory = entityManager.persist(testInventory);
        entityManager.flush();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(savedInventory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedInventory.getId());
    }

    /**
     * Test finding product inventory by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when inventory not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product inventory by ID.
     * Verifies that the inventory is removed from the database.
     */
    @Test
    @DisplayName("Should delete product inventory by ID successfully")
    void testDeleteById() {
        // Given
        ProductInventory savedInventory = entityManager.persist(testInventory);
        entityManager.flush();
        UUID savedId = savedInventory.getId();

        // When
        productInventoryRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all product inventories.
     * Verifies that all persisted inventories are retrieved.
     */
    @Test
    @DisplayName("Should find all product inventories")
    void testFindAll() {
        // Given
        entityManager.persist(testInventory);
        
        ProductInventory anotherInventory = new ProductInventory();
        anotherInventory.setProductId(UUID.randomUUID());
        anotherInventory.setQuantity(50);
        anotherInventory.setReservedQuantity(5);
        entityManager.persist(anotherInventory);
        entityManager.flush();

        // When
        var allInventories = productInventoryRepository.findAll();

        // Then
        assertThat(allInventories).hasSize(2);
    }

    /**
     * Test checking if product inventory exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when inventory exists by ID")
    void testExistsById_WhenExists() {
        // Given
        ProductInventory savedInventory = entityManager.persist(testInventory);
        entityManager.flush();

        // When
        boolean exists = productInventoryRepository.existsById(savedInventory.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if product inventory exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when inventory does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productInventoryRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all product inventories.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all product inventories correctly")
    void testCount() {
        // Given
        entityManager.persist(testInventory);
        ProductInventory anotherInventory = new ProductInventory();
        anotherInventory.setProductId(UUID.randomUUID());
        anotherInventory.setQuantity(75);
        entityManager.persist(anotherInventory);
        entityManager.flush();

        // When
        long count = productInventoryRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    /**
     * Test available quantity calculation.
     * Verifies that available quantity is correctly computed as quantity minus reserved.
     */
    @Test
    @DisplayName("Should correctly calculate available quantity")
    void testAvailableQuantity() {
        // Given
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(25);
        entityManager.persist(testInventory);
        entityManager.flush();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isPresent();
        int availableQuantity = result.get().getQuantity() - result.get().getReservedQuantity();
        assertThat(availableQuantity).isEqualTo(75);
    }
}