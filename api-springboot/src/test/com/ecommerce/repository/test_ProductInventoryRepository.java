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
 * Tests all repository methods for product inventory operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductInventoryRepository Tests")
public class test_ProductInventoryRepository {

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;
    private ProductInventory testInventory;
    private UUID productId;

    /**
     * Set up test data before each test method execution.
     * Creates test product and inventory entities.
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
        testInventory.setProduct(testProduct);
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(10);
        entityManager.persist(testInventory);

        entityManager.flush();
    }

    /**
     * Test finding product inventory by product ID.
     * Verifies that the correct inventory is retrieved for a given product.
     */
    @Test
    @DisplayName("Should find product inventory by product ID")
    void testFindByProductId_Success() {
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(productId);

        assertThat(result).isPresent();
        assertThat(result.get().getProduct().getProductId()).isEqualTo(productId);
        assertThat(result.get().getQuantity()).isEqualTo(100);
        assertThat(result.get().getReservedQuantity()).isEqualTo(10);
    }

    /**
     * Test finding product inventory with non-existent product ID.
     * Verifies that an empty Optional is returned when product ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when product ID not found")
    void testFindByProductId_NotFound() {
        UUID nonExistentProductId = UUID.randomUUID();

        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        assertThat(result).isEmpty();
    }

    /**
     * Test finding product inventory with null product ID.
     * Verifies proper handling of null product ID.
     */
    @Test
    @DisplayName("Should handle null product ID")
    void testFindByProductId_NullProductId() {
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(null);

        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new product inventory.
     * Verifies that inventory can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new product inventory successfully")
    void testSaveProductInventory() {
        Product newProduct = new Product();
        newProduct.setProductId(UUID.randomUUID());
        newProduct.setName("New Product");
        newProduct.setPrice(BigDecimal.valueOf(49.99));
        entityManager.persist(newProduct);

        ProductInventory newInventory = new ProductInventory();
        newInventory.setId(UUID.randomUUID());
        newInventory.setProduct(newProduct);
        newInventory.setQuantity(50);
        newInventory.setReservedQuantity(0);

        ProductInventory saved = productInventoryRepository.save(newInventory);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(50);
        assertThat(saved.getReservedQuantity()).isEqualTo(0);
    }

    /**
     * Test updating existing product inventory.
     * Verifies that inventory quantity can be updated.
     */
    @Test
    @DisplayName("Should update existing product inventory")
    void testUpdateProductInventory() {
        testInventory.setQuantity(150);
        testInventory.setReservedQuantity(20);
        ProductInventory updated = productInventoryRepository.save(testInventory);

        assertThat(updated.getQuantity()).isEqualTo(150);
        assertThat(updated.getReservedQuantity()).isEqualTo(20);
        assertThat(updated.getId()).isEqualTo(testInventory.getId());
    }

    /**
     * Test finding product inventory by ID.
     * Verifies that inventory can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find product inventory by ID")
    void testFindById_Success() {
        Optional<ProductInventory> result = productInventoryRepository.findById(testInventory.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testInventory.getId());
        assertThat(result.get().getQuantity()).isEqualTo(100);
    }

    /**
     * Test finding product inventory with non-existent ID.
     * Verifies that an empty Optional is returned when ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when inventory ID not found")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<ProductInventory> result = productInventoryRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    /**
     * Test deleting product inventory by ID.
     * Verifies that inventory can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete product inventory by ID")
    void testDeleteProductInventory() {
        UUID inventoryId = testInventory.getId();
        productInventoryRepository.deleteById(inventoryId);
        entityManager.flush();

        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all product inventories.
     * Verifies that all inventories can be retrieved.
     */
    @Test
    @DisplayName("Should find all product inventories")
    void testFindAll() {
        assertThat(productInventoryRepository.findAll()).isNotEmpty();
        assertThat(productInventoryRepository.findAll()).hasSize(1);
    }

    /**
     * Test inventory with zero quantity.
     * Verifies that inventory with zero quantity can be persisted.
     */
    @Test
    @DisplayName("Should handle inventory with zero quantity")
    void testInventoryWithZeroQuantity() {
        testInventory.setQuantity(0);
        testInventory.setReservedQuantity(0);
        ProductInventory updated = productInventoryRepository.save(testInventory);

        assertThat(updated.getQuantity()).isEqualTo(0);
        assertThat(updated.getReservedQuantity()).isEqualTo(0);
    }

    /**
     * Test inventory with negative reserved quantity.
     * Verifies that inventory with negative reserved quantity can be persisted.
     */
    @Test
    @DisplayName("Should handle inventory with negative reserved quantity")
    void testInventoryWithNegativeReservedQuantity() {
        testInventory.setReservedQuantity(-5);
        ProductInventory updated = productInventoryRepository.save(testInventory);

        assertThat(updated.getReservedQuantity()).isEqualTo(-5);
    }

    /**
     * Test counting product inventories.
     * Verifies that the count of inventories is accurate.
     */
    @Test
    @DisplayName("Should count product inventories correctly")
    void testCount() {
        long count = productInventoryRepository.count();

        assertThat(count).isEqualTo(1L);
    }
}