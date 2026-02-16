package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductRepository.
 * Tests all public methods including custom search query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("ProductRepository Tests")
public class test_ProductRepository {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("iPhone 15 Pro");
        testProduct.setDescription("Latest Apple smartphone with advanced features");
        testProduct.setPrice(BigDecimal.valueOf(999.99));
        testProduct.setStockQuantity(50);
    }

    /**
     * Test searching products by keyword in name - success case.
     */
    @Test
    @DisplayName("Should find products by keyword in name")
    void testSearchProducts_ByName() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("iPhone");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(p -> p.getName().contains("iPhone"));
    }

    /**
     * Test searching products by keyword in description.
     */
    @Test
    @DisplayName("Should find products by keyword in description")
    void testSearchProducts_ByDescription() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("smartphone");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(p -> p.getDescription().toLowerCase().contains("smartphone"));
    }

    /**
     * Test searching products with case-insensitive keyword.
     */
    @Test
    @DisplayName("Should search products case-insensitively")
    void testSearchProducts_CaseInsensitive() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> resultLower = productRepository.searchProducts("iphone");
        List<Product> resultUpper = productRepository.searchProducts("IPHONE");

        // Then
        assertThat(resultLower).isNotEmpty();
        assertThat(resultUpper).isNotEmpty();
        assertThat(resultLower.size()).isEqualTo(resultUpper.size());
    }

    /**
     * Test searching products with no results.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoResults() {
        // When
        List<Product> result = productRepository.searchProducts("NonExistentProduct");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test searching products with partial keyword.
     */
    @Test
    @DisplayName("Should find products with partial keyword match")
    void testSearchProducts_PartialMatch() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("iPh");

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test saving a product.
     */
    @Test
    @DisplayName("Should save product successfully")
    void testSaveProduct() {
        // When
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("iPhone 15 Pro");
    }

    /**
     * Test finding product by ID.
     */
    @Test
    @DisplayName("Should find product by ID")
    void testFindById_Success() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        // When
        Optional<Product> result = productRepository.findById(savedProduct.getProductId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(savedProduct.getProductId());
    }

    /**
     * Test finding product by ID - not found.
     */
    @Test
    @DisplayName("Should return empty when product not found by ID")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Product> result = productRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product.
     */
    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();
        UUID productId = savedProduct.getProductId();

        // When
        productRepository.deleteById(productId);
        entityManager.flush();

        // Then
        Optional<Product> result = productRepository.findById(productId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all products.
     */
    @Test
    @DisplayName("Should find all products")
    void testFindAll() {
        // Given
        productRepository.save(testProduct);
        
        Product anotherProduct = new Product();
        anotherProduct.setProductId(UUID.randomUUID());
        anotherProduct.setName("Samsung Galaxy S24");
        anotherProduct.setDescription("Android flagship smartphone");
        anotherProduct.setPrice(BigDecimal.valueOf(899.99));
        productRepository.save(anotherProduct);
        entityManager.flush();

        // When
        List<Product> products = productRepository.findAll();

        // Then
        assertThat(products).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test updating a product.
     */
    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        // When
        savedProduct.setPrice(BigDecimal.valueOf(1099.99));
        Product updatedProduct = productRepository.save(savedProduct);
        entityManager.flush();

        // Then
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1099.99));
    }

    /**
     * Test searching with empty keyword.
     */
    @Test
    @DisplayName("Should handle empty keyword search")
    void testSearchProducts_EmptyKeyword() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("");

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test searching with null keyword - edge case.
     */
    @Test
    @DisplayName("Should handle null keyword gracefully")
    void testSearchProducts_NullKeyword() {
        // When/Then - should not throw exception
        List<Product> result = productRepository.searchProducts(null);
        assertThat(result).isEmpty();
    }
}