package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductRepository.
 * Tests repository operations for Product entity including custom query methods.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Tests")
class test_ProductRepository {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
    }

    /**
     * Test searching products by keyword in name.
     * Verifies that products matching the keyword in name are returned.
     */
    @Test
    @DisplayName("Should find products by keyword in name")
    void testSearchProducts_ByNameKeyword_ShouldReturnMatchingProducts() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Product> result = productRepository.searchProducts("Test");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(p -> p.getName().contains("Test"));
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that products matching the keyword in description are returned.
     */
    @Test
    @DisplayName("Should find products by keyword in description")
    void testSearchProducts_ByDescriptionKeyword_ShouldReturnMatchingProducts() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Product> result = productRepository.searchProducts("Description");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(p -> p.getDescription().contains("Description"));
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that the search is case-insensitive.
     */
    @Test
    @DisplayName("Should perform case-insensitive search")
    void testSearchProducts_CaseInsensitive_ShouldReturnMatchingProducts() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Product> result = productRepository.searchProducts("test");

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test searching products with non-matching keyword.
     * Verifies that an empty list is returned when no products match.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoMatch_ShouldReturnEmptyList() {
        // When
        List<Product> result = productRepository.searchProducts("NonExistentKeyword");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test searching products with empty keyword.
     * Verifies behavior with empty search string.
     */
    @Test
    @DisplayName("Should handle empty keyword")
    void testSearchProducts_EmptyKeyword_ShouldReturnAllProducts() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("");

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test searching products with null keyword.
     * Verifies proper handling of null parameters.
     */
    @Test
    @DisplayName("Should handle null keyword gracefully")
    void testSearchProducts_NullKeyword_ShouldNotThrowException() {
        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            productRepository.searchProducts(null);
        });
    }

    /**
     * Test saving a product.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save product successfully")
    void testSave_ShouldPersistProduct() {
        // When
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
    }

    /**
     * Test finding a product by ID.
     * Verifies that findById returns the correct product.
     */
    @Test
    @DisplayName("Should find product by ID when exists")
    void testFindById_WhenExists_ShouldReturnProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();
        UUID savedId = savedProduct.getProductId();

        // When
        Optional<Product> result = productRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(savedId);
    }

    /**
     * Test finding a product by ID when it does not exist.
     * Verifies that findById returns empty Optional.
     */
    @Test
    @DisplayName("Should return empty when product ID does not exist")
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Product> result = productRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete product successfully")
    void testDelete_ShouldRemoveProduct() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        entityManager.flush();
        UUID savedId = savedProduct.getProductId();

        // When
        productRepository.delete(savedProduct);
        entityManager.flush();

        // Then
        Optional<Product> result = productRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all products.
     * Verifies that findAll returns all persisted products.
     */
    @Test
    @DisplayName("Should find all products")
    void testFindAll_ShouldReturnAllProducts() {
        // Given
        productRepository.save(testProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.findAll();

        // Then
        assertThat(result).isNotEmpty();
    }
}