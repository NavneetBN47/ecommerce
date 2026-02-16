package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for ProductInventoryRepository.
 * Tests repository methods for ProductInventory entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductInventory Repository Tests")
class test_ProductInventoryRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    private Product testProduct;
    private ProductInventory testInventory;
    private UUID productId;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities in the in-memory database.
     */
    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);

        testInventory = new ProductInventory();
        testInventory.setId(UUID.randomUUID());
        testInventory.setProductId(productId);
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(10);
        entityManager.persist(testInventory);

        entityManager.flush();
    }

    /**
     * Test finding ProductInventory by product ID.
     * Verifies that the custom query method returns the correct inventory for a product.
     */
    @Test
    @DisplayName("Should find ProductInventory by product ID")
    void testFindByProductId_Success() {
        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(productId);
        assertThat(result.get().getQuantity()).isEqualTo(100);
        assertThat(result.get().getReservedQuantity()).isEqualTo(10);
    }

    /**
     * Test finding ProductInventory with non-existent product ID.
     * Verifies that the method returns empty Optional when product doesn't have inventory.
     */
    @Test
    @DisplayName("Should return empty Optional when product ID does not exist")
    void testFindByProductId_NotFound() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new ProductInventory.
     * Verifies that the repository can persist a new ProductInventory entity.
     */
    @Test
    @DisplayName("Should save a new ProductInventory successfully")
    void testSave_NewInventory() {
        // Given
        UUID newProductId = UUID.randomUUID();
        Product newProduct = new Product();
        newProduct.setProductId(newProductId);
        newProduct.setName("New Product");
        newProduct.setPrice(BigDecimal.valueOf(149.99));
        entityManager.persist(newProduct);

        ProductInventory newInventory = new ProductInventory();
        newInventory.setId(UUID.randomUUID());
        newInventory.setProductId(newProductId);
        newInventory.setQuantity(50);
        newInventory.setReservedQuantity(5);

        // When
        ProductInventory savedInventory = productInventoryRepository.save(newInventory);

        // Then
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getId()).isNotNull();
        assertThat(savedInventory.getProductId()).isEqualTo(newProductId);
        assertThat(savedInventory.getQuantity()).isEqualTo(50);
    }

    /**
     * Test updating an existing ProductInventory.
     * Verifies that the repository can update inventory quantities.
     */
    @Test
    @DisplayName("Should update existing ProductInventory successfully")
    void testSave_UpdateInventory() {
        // Given
        testInventory.setQuantity(200);
        testInventory.setReservedQuantity(20);

        // When
        ProductInventory updatedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(updatedInventory.getQuantity()).isEqualTo(200);
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(20);
    }

    /**
     * Test finding ProductInventory by ID.
     * Verifies that the repository can retrieve inventory by its primary key.
     */
    @Test
    @DisplayName("Should find ProductInventory by ID")
    void testFindById_Success() {
        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(testInventory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testInventory.getId());
    }

    /**
     * Test finding ProductInventory with non-existent ID.
     * Verifies that the method returns empty Optional for non-existent inventory.
     */
    @Test
    @DisplayName("Should return empty Optional when inventory ID does not exist")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting ProductInventory.
     * Verifies that the repository can delete an inventory entity.
     */
    @Test
    @DisplayName("Should delete ProductInventory successfully")
    void testDelete_Success() {
        // Given
        UUID inventoryId = testInventory.getId();

        // When
        productInventoryRepository.delete(testInventory);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all ProductInventory records.
     * Verifies that the repository can count the total number of inventory records.
     */
    @Test
    @DisplayName("Should count all ProductInventory records")
    void testCount_Success() {
        // When
        long count = productInventoryRepository.count();

        // Then
        assertThat(count).isGreaterThan(0);
    }

    /**
     * Test checking if ProductInventory exists by ID.
     * Verifies that the repository can check existence of inventory.
     */
    @Test
    @DisplayName("Should return true when ProductInventory exists")
    void testExistsById_True() {
        // When
        boolean exists = productInventoryRepository.existsById(testInventory.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if ProductInventory exists with non-existent ID.
     * Verifies that the repository returns false for non-existent inventory.
     */
    @Test
    @DisplayName("Should return false when ProductInventory does not exist")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productInventoryRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test saving ProductInventory with zero quantity.
     * Verifies that the repository can handle zero quantity edge case.
     */
    @Test
    @DisplayName("Should save ProductInventory with zero quantity")
    void testSave_ZeroQuantity() {
        // Given
        testInventory.setQuantity(0);
        testInventory.setReservedQuantity(0);

        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(savedInventory.getQuantity()).isEqualTo(0);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(0);
    }

    /**
     * Test saving ProductInventory with large quantity.
     * Verifies that the repository can handle large quantity values.
     */
    @Test
    @DisplayName("Should save ProductInventory with large quantity")
    void testSave_LargeQuantity() {
        // Given
        testInventory.setQuantity(1000000);
        testInventory.setReservedQuantity(50000);

        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(savedInventory.getQuantity()).isEqualTo(1000000);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(50000);
    }

    /**
     * Test finding inventory for multiple products.
     * Verifies that each product has its own separate inventory.
     */
    @Test
    @DisplayName("Should find separate inventory for different products")
    void testFindByProductId_MultipleProducts() {
        // Given
        UUID productId2 = UUID.randomUUID();
        Product product2 = new Product();
        product2.setProductId(productId2);
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(199.99));
        entityManager.persist(product2);

        ProductInventory inventory2 = new ProductInventory();
        inventory2.setId(UUID.randomUUID());
        inventory2.setProductId(productId2);
        inventory2.setQuantity(75);
        inventory2.setReservedQuantity(15);
        entityManager.persist(inventory2);
        entityManager.flush();

        // When
        Optional<ProductInventory> result1 = productInventoryRepository.findByProductId(productId);
        Optional<ProductInventory> result2 = productInventoryRepository.findByProductId(productId2);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().getProductId()).isEqualTo(productId);
        assertThat(result2.get().getProductId()).isEqualTo(productId2);
        assertThat(result1.get().getQuantity()).isEqualTo(100);
        assertThat(result2.get().getQuantity()).isEqualTo(75);
    }

    /**
     * Test updating reserved quantity independently.
     * Verifies that reserved quantity can be updated without affecting total quantity.
     */
    @Test
    @DisplayName("Should update reserved quantity independently")
    void testSave_UpdateReservedQuantity() {
        // Given
        int originalQuantity = testInventory.getQuantity();
        testInventory.setReservedQuantity(30);

        // When
        ProductInventory updatedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(updatedInventory.getQuantity()).isEqualTo(originalQuantity);
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(30);
    }
}