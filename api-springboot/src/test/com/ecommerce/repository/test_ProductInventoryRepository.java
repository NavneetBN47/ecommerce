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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductInventoryRepository.
 * Tests repository methods for ProductInventory entity operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
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
     * Creates and persists test entities for Product and ProductInventory.
     */
    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        // Create test product
        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        entityManager.persist(testProduct);

        // Create test inventory
        testInventory = new ProductInventory();
        testInventory.setProductId(productId);
        testInventory.setQuantity(100);
        testInventory.setReservedQuantity(10);
        testInventory.setAvailableQuantity(90);
        entityManager.persist(testInventory);
        
        entityManager.flush();
    }

    /**
     * Test finding ProductInventory by product ID when it exists.
     * Verifies that the correct ProductInventory is returned.
     */
    @Test
    @DisplayName("Should find ProductInventory by product ID when exists")
    void testFindByProductId_WhenExists_ReturnsInventory() {
        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(productId);
        assertThat(result.get().getQuantity()).isEqualTo(100);
        assertThat(result.get().getReservedQuantity()).isEqualTo(10);
        assertThat(result.get().getAvailableQuantity()).isEqualTo(90);
    }

    /**
     * Test finding ProductInventory by product ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when ProductInventory does not exist")
    void testFindByProductId_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new ProductInventory.
     * Verifies that the ProductInventory is persisted correctly.
     */
    @Test
    @DisplayName("Should save new ProductInventory successfully")
    void testSave_NewInventory_Success() {
        // Given
        UUID newProductId = UUID.randomUUID();
        Product newProduct = new Product();
        newProduct.setProductId(newProductId);
        newProduct.setName("New Product");
        newProduct.setPrice(149.99);
        entityManager.persist(newProduct);

        ProductInventory newInventory = new ProductInventory();
        newInventory.setProductId(newProductId);
        newInventory.setQuantity(50);
        newInventory.setReservedQuantity(5);
        newInventory.setAvailableQuantity(45);

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
     * Verifies that inventory quantity updates are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing ProductInventory successfully")
    void testSave_UpdateInventory_Success() {
        // Given
        testInventory.setQuantity(150);
        testInventory.setReservedQuantity(20);
        testInventory.setAvailableQuantity(130);

        // When
        ProductInventory updatedInventory = productInventoryRepository.save(testInventory);
        entityManager.flush();

        // Then
        assertThat(updatedInventory.getQuantity()).isEqualTo(150);
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(20);
        assertThat(updatedInventory.getAvailableQuantity()).isEqualTo(130);
    }

    /**
     * Test deleting ProductInventory by ID.
     * Verifies that the ProductInventory is removed from the database.
     */
    @Test
    @DisplayName("Should delete ProductInventory by ID successfully")
    void testDeleteById_Success() {
        // Given
        UUID inventoryId = testInventory.getId();

        // When
        productInventoryRepository.deleteById(inventoryId);
        entityManager.flush();

        // Then
        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding ProductInventory by ID.
     * Verifies that the correct ProductInventory is retrieved.
     */
    @Test
    @DisplayName("Should find ProductInventory by ID when exists")
    void testFindById_WhenExists_ReturnsInventory() {
        // Given
        UUID inventoryId = testInventory.getId();

        // When
        Optional<ProductInventory> result = productInventoryRepository.findById(inventoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(inventoryId);
        assertThat(result.get().getProductId()).isEqualTo(productId);
    }

    /**
     * Test saving ProductInventory with zero quantity.
     * Verifies that edge case values are handled correctly.
     */
    @Test
    @DisplayName("Should save ProductInventory with zero quantity")
    void testSave_WithZeroQuantity_Success() {
        // Given
        testInventory.setQuantity(0);
        testInventory.setReservedQuantity(0);
        testInventory.setAvailableQuantity(0);

        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(savedInventory.getQuantity()).isEqualTo(0);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(0);
        assertThat(savedInventory.getAvailableQuantity()).isEqualTo(0);
    }

    /**
     * Test saving ProductInventory with large quantity.
     * Verifies that large values are handled correctly.
     */
    @Test
    @DisplayName("Should save ProductInventory with large quantity")
    void testSave_WithLargeQuantity_Success() {
        // Given
        testInventory.setQuantity(1000000);
        testInventory.setReservedQuantity(100000);
        testInventory.setAvailableQuantity(900000);

        // When
        ProductInventory savedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(savedInventory.getQuantity()).isEqualTo(1000000);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(100000);
        assertThat(savedInventory.getAvailableQuantity()).isEqualTo(900000);
    }

    /**
     * Test updating reserved quantity.
     * Verifies that reserved quantity can be updated independently.
     */
    @Test
    @DisplayName("Should update reserved quantity successfully")
    void testUpdate_ReservedQuantity_Success() {
        // Given
        int newReservedQuantity = 25;
        testInventory.setReservedQuantity(newReservedQuantity);
        testInventory.setAvailableQuantity(testInventory.getQuantity() - newReservedQuantity);

        // When
        ProductInventory updatedInventory = productInventoryRepository.save(testInventory);

        // Then
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(newReservedQuantity);
        assertThat(updatedInventory.getAvailableQuantity()).isEqualTo(75);
    }

    /**
     * Test that inventory maintains relationship with product.
     * Verifies product-inventory relationship integrity.
     */
    @Test
    @DisplayName("Should maintain relationship with Product")
    void testInventoryProductRelationship() {
        // When
        Optional<ProductInventory> result = productInventoryRepository.findByProductId(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(productId);
        assertThat(result.get().getProductId()).isEqualTo(testProduct.getProductId());
    }
}