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
 * Tests repository methods for Product entity operations including search functionality.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Tests")
public class test_ProductRepository {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product product1;
    private Product product2;
    private Product product3;

    /**
     * Set up test data before each test method execution.
     * Creates and persists multiple test products with various attributes.
     */
    @BeforeEach
    void setUp() {
        // Create test products
        product1 = new Product();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Laptop Computer");
        product1.setDescription("High-performance laptop for professionals");
        product1.setPrice(1299.99);
        entityManager.persist(product1);

        product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Wireless Mouse");
        product2.setDescription("Ergonomic wireless mouse with precision tracking");
        product2.setPrice(29.99);
        entityManager.persist(product2);

        product3 = new Product();
        product3.setProductId(UUID.randomUUID());
        product3.setName("Smartphone");
        product3.setDescription("Latest smartphone with advanced camera");
        product3.setPrice(899.99);
        entityManager.persist(product3);
        
        entityManager.flush();
    }

    /**
     * Test searching products by keyword in name.
     * Verifies that products matching the keyword in name are returned.
     */
    @Test
    @DisplayName("Should search products by keyword in name")
    void testSearchProducts_ByNameKeyword_ReturnsMatchingProducts() {
        // When
        List<Product> result = productRepository.searchProducts("laptop");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("laptop");
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that products matching the keyword in description are returned.
     */
    @Test
    @DisplayName("Should search products by keyword in description")
    void testSearchProducts_ByDescriptionKeyword_ReturnsMatchingProducts() {
        // When
        List<Product> result = productRepository.searchProducts("wireless");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).containsIgnoringCase("wireless");
    }

    /**
     * Test searching products with partial keyword match.
     * Verifies that partial matches are found correctly.
     */
    @Test
    @DisplayName("Should search products with partial keyword")
    void testSearchProducts_PartialKeyword_ReturnsMatchingProducts() {
        // When
        List<Product> result = productRepository.searchProducts("mou");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).containsIgnoringCase("mouse");
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that search is case-insensitive.
     */
    @Test
    @DisplayName("Should search products case-insensitively")
    void testSearchProducts_CaseInsensitive_ReturnsMatchingProducts() {
        // When
        List<Product> resultLower = productRepository.searchProducts("smartphone");
        List<Product> resultUpper = productRepository.searchProducts("SMARTPHONE");
        List<Product> resultMixed = productRepository.searchProducts("SmArTpHoNe");

        // Then
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
        assertThat(resultLower.get(0).getProductId()).isEqualTo(resultUpper.get(0).getProductId());
    }

    /**
     * Test searching products with no matching keyword.
     * Verifies that an empty list is returned when no matches are found.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoMatch_ReturnsEmpty() {
        // When
        List<Product> result = productRepository.searchProducts("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test searching products with empty keyword.
     * Verifies behavior with empty search string.
     */
    @Test
    @DisplayName("Should handle empty keyword search")
    void testSearchProducts_EmptyKeyword_ReturnsAllProducts() {
        // When
        List<Product> result = productRepository.searchProducts("");

        // Then
        assertThat(result).hasSize(3);
    }

    /**
     * Test saving a new Product.
     * Verifies that the Product is persisted correctly.
     */
    @Test
    @DisplayName("Should save new Product successfully")
    void testSave_NewProduct_Success() {
        // Given
        Product newProduct = new Product();
        newProduct.setProductId(UUID.randomUUID());
        newProduct.setName("Tablet");
        newProduct.setDescription("Portable tablet device");
        newProduct.setPrice(499.99);

        // When
        Product savedProduct = productRepository.save(newProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Tablet");
    }

    /**
     * Test updating an existing Product.
     * Verifies that Product modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing Product successfully")
    void testSave_UpdateProduct_Success() {
        // Given
        product1.setPrice(1199.99);
        product1.setDescription("Updated description");

        // When
        Product updatedProduct = productRepository.save(product1);
        entityManager.flush();

        // Then
        assertThat(updatedProduct.getPrice()).isEqualTo(1199.99);
        assertThat(updatedProduct.getDescription()).isEqualTo("Updated description");
    }

    /**
     * Test finding a Product by ID.
     * Verifies that the correct Product is retrieved.
     */
    @Test
    @DisplayName("Should find Product by ID when exists")
    void testFindById_WhenExists_ReturnsProduct() {
        // When
        Optional<Product> result = productRepository.findById(product1.getProductId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product1.getProductId());
        assertThat(result.get().getName()).isEqualTo("Laptop Computer");
    }

    /**
     * Test finding a Product by ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when Product ID does not exist")
    void testFindById_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Product> result = productRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Product by ID.
     * Verifies that the Product is removed from the database.
     */
    @Test
    @DisplayName("Should delete Product by ID successfully")
    void testDeleteById_Success() {
        // Given
        UUID productId = product3.getProductId();

        // When
        productRepository.deleteById(productId);
        entityManager.flush();

        // Then
        Optional<Product> result = productRepository.findById(productId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all Products.
     * Verifies that all persisted Products are retrieved.
     */
    @Test
    @DisplayName("Should find all Products")
    void testFindAll_ReturnsAllProducts() {
        // When
        List<Product> allProducts = productRepository.findAll();

        // Then
        assertThat(allProducts).isNotEmpty();
        assertThat(allProducts).hasSize(3);
    }

    /**
     * Test searching products matches both name and description.
     * Verifies that search works across multiple fields.
     */
    @Test
    @DisplayName("Should search across both name and description")
    void testSearchProducts_AcrossMultipleFields_ReturnsMatches() {
        // When - keyword exists in different fields for different products
        List<Product> result = productRepository.searchProducts("camera");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getDescription()).containsIgnoringCase("camera");
    }

    /**
     * Test searching products with special characters.
     * Verifies that special characters in search are handled correctly.
     */
    @Test
    @DisplayName("Should handle special characters in search keyword")
    void testSearchProducts_WithSpecialCharacters_HandlesCorrectly() {
        // Given
        Product specialProduct = new Product();
        specialProduct.setProductId(UUID.randomUUID());
        specialProduct.setName("Product-123");
        specialProduct.setDescription("Special product with numbers");
        specialProduct.setPrice(99.99);
        entityManager.persist(specialProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("123");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).contains("123");
    }
}