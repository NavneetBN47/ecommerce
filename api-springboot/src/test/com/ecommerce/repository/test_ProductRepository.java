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
 * Tests repository methods for Product entity operations including custom search queries.
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
        testProduct.setName("Laptop Computer");
        testProduct.setDescription("High performance laptop for professionals");
        testProduct.setPrice(BigDecimal.valueOf(1299.99));
    }

    /**
     * Test searching products by keyword in name.
     * Verifies that the repository correctly searches products by name keyword.
     */
    @Test
    @DisplayName("Should search products by keyword in name")
    void testSearchProducts_ByNameKeyword() {
        // Given
        entityManager.persistAndFlush(testProduct);

        // When
        List<Product> results = productRepository.searchProducts("Laptop");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(p -> p.getName().contains("Laptop"));
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that the repository correctly searches products by description keyword.
     */
    @Test
    @DisplayName("Should search products by keyword in description")
    void testSearchProducts_ByDescriptionKeyword() {
        // Given
        entityManager.persistAndFlush(testProduct);

        // When
        List<Product> results = productRepository.searchProducts("performance");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(p -> p.getDescription().toLowerCase().contains("performance"));
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that the search is case-insensitive.
     */
    @Test
    @DisplayName("Should search products case-insensitively")
    void testSearchProducts_CaseInsensitive() {
        // Given
        entityManager.persistAndFlush(testProduct);

        // When
        List<Product> results = productRepository.searchProducts("LAPTOP");

        // Then
        assertThat(results).isNotEmpty();
    }

    /**
     * Test searching products with non-existent keyword.
     * Verifies that the repository returns empty list for non-matching keyword.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoMatches() {
        // Given
        entityManager.persistAndFlush(testProduct);

        // When
        List<Product> results = productRepository.searchProducts("NonExistentKeyword");

        // Then
        assertThat(results).isEmpty();
    }

    /**
     * Test searching products with partial keyword.
     * Verifies that the repository supports partial keyword matching.
     */
    @Test
    @DisplayName("Should search products with partial keyword")
    void testSearchProducts_PartialKeyword() {
        // Given
        entityManager.persistAndFlush(testProduct);

        // When
        List<Product> results = productRepository.searchProducts("Lap");

        // Then
        assertThat(results).isNotEmpty();
    }

    /**
     * Test saving a product.
     * Verifies that the repository correctly persists a product.
     */
    @Test
    @DisplayName("Should save product successfully")
    void testSave_Product() {
        // When
        Product savedProduct = productRepository.save(testProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop Computer");
    }

    /**
     * Test finding a product by ID.
     * Verifies that the repository correctly retrieves a product by its ID.
     */
    @Test
    @DisplayName("Should find product by ID")
    void testFindById_WhenExists() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct);

        // When
        Optional<Product> result = productRepository.findById(savedProduct.getProductId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(savedProduct.getProductId());
    }

    /**
     * Test finding a product by ID when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent product.
     */
    @Test
    @DisplayName("Should return empty when product does not exist")
    void testFindById_WhenNotExists() {
        // When
        Optional<Product> result = productRepository.findById(UUID.randomUUID());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product.
     * Verifies that the repository correctly deletes a product.
     */
    @Test
    @DisplayName("Should delete product successfully")
    void testDelete_Product() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct);
        UUID productId = savedProduct.getProductId();

        // When
        productRepository.delete(savedProduct);
        entityManager.flush();

        // Then
        Optional<Product> result = productRepository.findById(productId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all products.
     * Verifies that the repository correctly retrieves all products.
     */
    @Test
    @DisplayName("Should find all products")
    void testFindAll_Products() {
        // Given
        entityManager.persistAndFlush(testProduct);
        Product anotherProduct = new Product();
        anotherProduct.setProductId(UUID.randomUUID());
        anotherProduct.setName("Desktop Computer");
        anotherProduct.setDescription("Powerful desktop for gaming");
        anotherProduct.setPrice(BigDecimal.valueOf(1599.99));
        entityManager.persistAndFlush(anotherProduct);

        // When
        List<Product> allProducts = productRepository.findAll();

        // Then
        assertThat(allProducts).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test updating a product.
     * Verifies that the repository correctly updates product details.
     */
    @Test
    @DisplayName("Should update product successfully")
    void testUpdate_Product() {
        // Given
        Product savedProduct = entityManager.persistAndFlush(testProduct);
        savedProduct.setPrice(BigDecimal.valueOf(1199.99));

        // When
        Product updatedProduct = productRepository.save(savedProduct);

        // Then
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1199.99));
    }
}