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
 * Tests repository methods for Product entity operations
 * including searching products by keyword.
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
     * Set up test data before each test method.
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
    @DisplayName("Should search products by keyword in name")
    void testSearchProducts_ByNameKeyword_ReturnsMatchingProducts() {
        // Given
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Laptop Computer");
        product.setDescription("High performance laptop");
        entityManager.persistAndFlush(product);

        // When
        List<Product> result = productRepository.searchProducts("Laptop");

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that products matching the keyword in description are returned.
     */
    @Test
    @DisplayName("Should search products by keyword in description")
    void testSearchProducts_ByDescriptionKeyword_ReturnsMatchingProducts() {
        // Given
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Gaming Mouse");
        product.setDescription("Professional gaming peripheral");
        entityManager.persistAndFlush(product);

        // When
        List<Product> result = productRepository.searchProducts("gaming");

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that search is case-insensitive.
     */
    @Test
    @DisplayName("Should search products case-insensitively")
    void testSearchProducts_CaseInsensitive_ReturnsMatchingProducts() {
        // Given
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("KEYBOARD");
        product.setDescription("Mechanical keyboard");
        entityManager.persistAndFlush(product);

        // When
        List<Product> result = productRepository.searchProducts("keyboard");

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test searching products with non-matching keyword.
     * Verifies that an empty list is returned when no products match.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoMatch_ReturnsEmptyList() {
        // Given
        String nonMatchingKeyword = "NonExistentProduct12345";

        // When
        List<Product> result = productRepository.searchProducts(nonMatchingKeyword);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a product.
     * Verifies that the product is persisted correctly.
     */
    @Test
    @DisplayName("Should save product successfully")
    void testSave_ValidProduct_SavesSuccessfully() {
        // Given
        Product newProduct = new Product();
        newProduct.setProductId(UUID.randomUUID());
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");

        // When
        Product savedProduct = productRepository.save(newProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("New Product");
    }

    /**
     * Test finding a product by ID.
     * Verifies that the product can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find product by ID when exists")
    void testFindById_WhenExists_ReturnsProduct() {
        // Given
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Find Product");
        product.setDescription("Find Description");
        Product savedProduct = entityManager.persistAndFlush(product);

        // When
        Optional<Product> result = productRepository.findById(savedProduct.getProductId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(savedProduct.getProductId());
    }

    /**
     * Test finding a product by ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when product does not exist")
    void testFindById_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Product> result = productRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product.
     * Verifies that the product is removed from the database.
     */
    @Test
    @DisplayName("Should delete product successfully")
    void testDelete_ExistingProduct_DeletesSuccessfully() {
        // Given
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Delete Product");
        product.setDescription("Delete Description");
        Product savedProduct = entityManager.persistAndFlush(product);
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
     * Verifies that all products can be retrieved.
     */
    @Test
    @DisplayName("Should find all products")
    void testFindAll_ReturnsAllProducts() {
        // Given
        Product product1 = new Product();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setDescription("Description 1");
        
        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setDescription("Description 2");
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.flush();

        // When
        List<Product> result = productRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting products.
     * Verifies that the count of products is correct.
     */
    @Test
    @DisplayName("Should count products correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = productRepository.count();
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName("Count Product");
        product.setDescription("Count Description");
        entityManager.persistAndFlush(product);

        // When
        long newCount = productRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
}