package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Tests")
class test_ProductRepository {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setName("Laptop Computer");
        testProduct1.setDescription("High performance laptop with SSD storage");
        testProduct1.setPrice(BigDecimal.valueOf(999.99));
        
        testProduct2 = new Product();
        testProduct2.setName("Wireless Mouse");
        testProduct2.setDescription("Ergonomic wireless mouse with USB receiver");
        testProduct2.setPrice(BigDecimal.valueOf(29.99));
        
        testProduct3 = new Product();
        testProduct3.setName("Mechanical Keyboard");
        testProduct3.setDescription("RGB mechanical keyboard for gaming");
        testProduct3.setPrice(BigDecimal.valueOf(149.99));
    }

    /**
     * Test searching products by keyword in name.
     * Verifies that products matching the keyword in name are returned.
     */
    @Test
    @DisplayName("Should find products by keyword in name")
    void testSearchProducts_ByName() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("laptop");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("laptop");
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that products matching the keyword in description are returned.
     */
    @Test
    @DisplayName("Should find products by keyword in description")
    void testSearchProducts_ByDescription() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("wireless");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).containsIgnoringCase("wireless");
    }

    /**
     * Test searching products with partial keyword match.
     * Verifies that partial matches are found.
     */
    @Test
    @DisplayName("Should find products with partial keyword match")
    void testSearchProducts_PartialMatch() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("key");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("keyboard");
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that search is case-insensitive.
     */
    @Test
    @DisplayName("Should find products with case-insensitive search")
    void testSearchProducts_CaseInsensitive() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();

        // When
        List<Product> resultLower = productRepository.searchProducts("mouse");
        List<Product> resultUpper = productRepository.searchProducts("MOUSE");
        List<Product> resultMixed = productRepository.searchProducts("MoUsE");

        // Then
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
    }

    /**
     * Test searching products with no matching results.
     * Verifies that an empty list is returned when no products match.
     */
    @Test
    @DisplayName("Should return empty list when no products match search")
    void testSearchProducts_NoMatch() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("smartphone");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test searching products with empty keyword.
     * Verifies that all products are returned when keyword is empty.
     */
    @Test
    @DisplayName("Should return all products when search keyword is empty")
    void testSearchProducts_EmptyKeyword() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("");

        // Then
        assertThat(result).hasSize(3);
    }

    /**
     * Test saving a new product.
     * Verifies that the product is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new product successfully")
    void testSave_NewProduct() {
        // When
        Product savedProduct = productRepository.save(testProduct1);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop Computer");
    }

    /**
     * Test updating an existing product.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing product successfully")
    void testSave_UpdateProduct() {
        // Given
        Product savedProduct = entityManager.persist(testProduct1);
        entityManager.flush();
        UUID savedId = savedProduct.getProductId();

        // When
        savedProduct.setPrice(BigDecimal.valueOf(899.99));
        Product updatedProduct = productRepository.save(savedProduct);

        // Then
        assertThat(updatedProduct.getProductId()).isEqualTo(savedId);
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(899.99));
    }

    /**
     * Test finding product by ID.
     * Verifies that the correct product is retrieved.
     */
    @Test
    @DisplayName("Should find product by ID when exists")
    void testFindById_WhenExists() {
        // Given
        Product savedProduct = entityManager.persist(testProduct1);
        entityManager.flush();

        // When
        Optional<Product> result = productRepository.findById(savedProduct.getProductId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(savedProduct.getProductId());
    }

    /**
     * Test finding product by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when product not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Product> result = productRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product by ID.
     * Verifies that the product is removed from the database.
     */
    @Test
    @DisplayName("Should delete product by ID successfully")
    void testDeleteById() {
        // Given
        Product savedProduct = entityManager.persist(testProduct1);
        entityManager.flush();
        UUID savedId = savedProduct.getProductId();

        // When
        productRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<Product> result = productRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all products.
     * Verifies that all persisted products are retrieved.
     */
    @Test
    @DisplayName("Should find all products")
    void testFindAll() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();

        // When
        List<Product> allProducts = productRepository.findAll();

        // Then
        assertThat(allProducts).hasSize(3);
    }

    /**
     * Test checking if product exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when product exists by ID")
    void testExistsById_WhenExists() {
        // Given
        Product savedProduct = entityManager.persist(testProduct1);
        entityManager.flush();

        // When
        boolean exists = productRepository.existsById(savedProduct.getProductId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if product exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when product does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test counting all products.
     * Verifies that the count is accurate.
     */
    @Test
    @DisplayName("Should count all products correctly")
    void testCount() {
        // Given
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.flush();

        // When
        long count = productRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}