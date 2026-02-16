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
 * JUnit test class for ProductRepository.
 * Tests repository methods for Product entity operations including custom search queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Product Repository Tests")
class test_ProductRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test products with different attributes.
     */
    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setProductId(UUID.randomUUID());
        testProduct1.setName("Laptop Computer");
        testProduct1.setDescription("High-performance laptop for professionals");
        testProduct1.setPrice(BigDecimal.valueOf(1299.99));
        entityManager.persist(testProduct1);

        testProduct2 = new Product();
        testProduct2.setProductId(UUID.randomUUID());
        testProduct2.setName("Wireless Mouse");
        testProduct2.setDescription("Ergonomic wireless mouse with precision tracking");
        testProduct2.setPrice(BigDecimal.valueOf(29.99));
        entityManager.persist(testProduct2);

        testProduct3 = new Product();
        testProduct3.setProductId(UUID.randomUUID());
        testProduct3.setName("Mechanical Keyboard");
        testProduct3.setDescription("RGB mechanical keyboard for gaming");
        testProduct3.setPrice(BigDecimal.valueOf(149.99));
        entityManager.persist(testProduct3);

        entityManager.flush();
    }

    /**
     * Test searching products by keyword in name.
     * Verifies that the custom search query finds products matching the keyword in name.
     */
    @Test
    @DisplayName("Should search products by keyword in name")
    void testSearchProducts_ByName() {
        // When
        List<Product> result = productRepository.searchProducts("Laptop");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("Laptop");
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that the custom search query finds products matching the keyword in description.
     */
    @Test
    @DisplayName("Should search products by keyword in description")
    void testSearchProducts_ByDescription() {
        // When
        List<Product> result = productRepository.searchProducts("wireless");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).containsIgnoringCase("wireless");
    }

    /**
     * Test searching products with partial keyword.
     * Verifies that the search supports partial matching.
     */
    @Test
    @DisplayName("Should search products with partial keyword")
    void testSearchProducts_PartialMatch() {
        // When
        List<Product> result = productRepository.searchProducts("key");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).containsIgnoringCase("keyboard");
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that the search is case-insensitive.
     */
    @Test
    @DisplayName("Should search products case-insensitively")
    void testSearchProducts_CaseInsensitive() {
        // When
        List<Product> resultLower = productRepository.searchProducts("laptop");
        List<Product> resultUpper = productRepository.searchProducts("LAPTOP");
        List<Product> resultMixed = productRepository.searchProducts("LaPtOp");

        // Then
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
        assertThat(resultLower.get(0).getProductId()).isEqualTo(resultUpper.get(0).getProductId());
    }

    /**
     * Test searching products with keyword matching multiple products.
     * Verifies that the search returns all matching products.
     */
    @Test
    @DisplayName("Should search and return multiple matching products")
    void testSearchProducts_MultipleMatches() {
        // Given
        Product product4 = new Product();
        product4.setProductId(UUID.randomUUID());
        product4.setName("Gaming Mouse");
        product4.setDescription("High DPI gaming mouse");
        product4.setPrice(BigDecimal.valueOf(79.99));
        entityManager.persist(product4);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("mouse");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName)
                .allMatch(name -> name.toLowerCase().contains("mouse"));
    }

    /**
     * Test searching products with non-existent keyword.
     * Verifies that the search returns empty list when no products match.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoMatch() {
        // When
        List<Product> result = productRepository.searchProducts("smartphone");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test searching products with empty keyword.
     * Verifies that the search handles empty keyword appropriately.
     */
    @Test
    @DisplayName("Should handle empty keyword search")
    void testSearchProducts_EmptyKeyword() {
        // When
        List<Product> result = productRepository.searchProducts("");

        // Then
        assertThat(result).hasSize(3);
    }

    /**
     * Test saving a new Product.
     * Verifies that the repository can persist a new Product entity.
     */
    @Test
    @DisplayName("Should save a new Product successfully")
    void testSave_NewProduct() {
        // Given
        Product newProduct = new Product();
        newProduct.setProductId(UUID.randomUUID());
        newProduct.setName("USB Cable");
        newProduct.setDescription("High-speed USB-C cable");
        newProduct.setPrice(BigDecimal.valueOf(19.99));

        // When
        Product savedProduct = productRepository.save(newProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("USB Cable");
    }

    /**
     * Test updating an existing Product.
     * Verifies that the repository can update Product properties.
     */
    @Test
    @DisplayName("Should update existing Product successfully")
    void testSave_UpdateProduct() {
        // Given
        testProduct1.setPrice(BigDecimal.valueOf(1199.99));
        testProduct1.setDescription("Updated description");

        // When
        Product updatedProduct = productRepository.save(testProduct1);

        // Then
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1199.99));
        assertThat(updatedProduct.getDescription()).isEqualTo("Updated description");
    }

    /**
     * Test finding a Product by ID.
     * Verifies that the repository can retrieve a Product by its primary key.
     */
    @Test
    @DisplayName("Should find Product by ID")
    void testFindById_Success() {
        // When
        Optional<Product> result = productRepository.findById(testProduct1.getProductId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(testProduct1.getProductId());
    }

    /**
     * Test finding a Product with non-existent ID.
     * Verifies that the method returns empty Optional for non-existent product.
     */
    @Test
    @DisplayName("Should return empty Optional when Product ID does not exist")
    void testFindById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Product> result = productRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Product.
     * Verifies that the repository can delete a Product entity.
     */
    @Test
    @DisplayName("Should delete Product successfully")
    void testDelete_Success() {
        // Given
        UUID productId = testProduct3.getProductId();

        // When
        productRepository.delete(testProduct3);
        entityManager.flush();

        // Then
        Optional<Product> result = productRepository.findById(productId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all Products.
     * Verifies that the repository can count the total number of Products.
     */
    @Test
    @DisplayName("Should count all Products")
    void testCount_Success() {
        // When
        long count = productRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    /**
     * Test checking if Product exists by ID.
     * Verifies that the repository can check existence of a Product.
     */
    @Test
    @DisplayName("Should return true when Product exists")
    void testExistsById_True() {
        // When
        boolean exists = productRepository.existsById(testProduct1.getProductId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if Product exists with non-existent ID.
     * Verifies that the repository returns false for non-existent Product.
     */
    @Test
    @DisplayName("Should return false when Product does not exist")
    void testExistsById_False() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = productRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding all Products.
     * Verifies that the repository can retrieve all Products.
     */
    @Test
    @DisplayName("Should find all Products")
    void testFindAll_Success() {
        // When
        List<Product> result = productRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop Computer", "Wireless Mouse", "Mechanical Keyboard");
    }

    /**
     * Test searching products with special characters.
     * Verifies that the search handles special characters correctly.
     */
    @Test
    @DisplayName("Should handle special characters in search")
    void testSearchProducts_SpecialCharacters() {
        // Given
        Product specialProduct = new Product();
        specialProduct.setProductId(UUID.randomUUID());
        specialProduct.setName("Product-123");
        specialProduct.setDescription("Special & unique product");
        specialProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(specialProduct);
        entityManager.flush();

        // When
        List<Product> result = productRepository.searchProducts("Product-123");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Product-123");
    }
}