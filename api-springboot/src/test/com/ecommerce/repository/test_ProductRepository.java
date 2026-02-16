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
 * Tests all repository methods including custom query methods for product operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
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

    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    /**
     * Set up test data before each test method execution.
     * Creates multiple test product entities with different attributes.
     */
    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setProductId(UUID.randomUUID());
        testProduct1.setName("Laptop Computer");
        testProduct1.setDescription("High performance laptop for gaming");
        testProduct1.setPrice(BigDecimal.valueOf(1299.99));
        entityManager.persist(testProduct1);

        testProduct2 = new Product();
        testProduct2.setProductId(UUID.randomUUID());
        testProduct2.setName("Wireless Mouse");
        testProduct2.setDescription("Ergonomic wireless mouse with laptop compatibility");
        testProduct2.setPrice(BigDecimal.valueOf(29.99));
        entityManager.persist(testProduct2);

        testProduct3 = new Product();
        testProduct3.setProductId(UUID.randomUUID());
        testProduct3.setName("USB Keyboard");
        testProduct3.setDescription("Mechanical keyboard with RGB lighting");
        testProduct3.setPrice(BigDecimal.valueOf(79.99));
        entityManager.persist(testProduct3);

        entityManager.flush();
    }

    /**
     * Test searching products by keyword in name.
     * Verifies that products matching the keyword in name are retrieved.
     */
    @Test
    @DisplayName("Should search products by keyword in name")
    void testSearchProducts_ByName() {
        List<Product> results = productRepository.searchProducts("laptop");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName)
            .containsExactlyInAnyOrder("Laptop Computer", "Wireless Mouse");
    }

    /**
     * Test searching products by keyword in description.
     * Verifies that products matching the keyword in description are retrieved.
     */
    @Test
    @DisplayName("Should search products by keyword in description")
    void testSearchProducts_ByDescription() {
        List<Product> results = productRepository.searchProducts("gaming");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Laptop Computer");
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that search is case-insensitive.
     */
    @Test
    @DisplayName("Should search products case-insensitively")
    void testSearchProducts_CaseInsensitive() {
        List<Product> resultsLower = productRepository.searchProducts("wireless");
        List<Product> resultsUpper = productRepository.searchProducts("WIRELESS");
        List<Product> resultsMixed = productRepository.searchProducts("WiReLeSs");

        assertThat(resultsLower).hasSize(1);
        assertThat(resultsUpper).hasSize(1);
        assertThat(resultsMixed).hasSize(1);
        assertThat(resultsLower.get(0).getName()).isEqualTo("Wireless Mouse");
    }

    /**
     * Test searching products with partial keyword.
     * Verifies that partial keyword matching works correctly.
     */
    @Test
    @DisplayName("Should search products with partial keyword")
    void testSearchProducts_PartialKeyword() {
        List<Product> results = productRepository.searchProducts("key");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("USB Keyboard");
    }

    /**
     * Test searching products with non-matching keyword.
     * Verifies that empty list is returned when no products match.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoMatches() {
        List<Product> results = productRepository.searchProducts("smartphone");

        assertThat(results).isEmpty();
    }

    /**
     * Test searching products with empty keyword.
     * Verifies that all products are returned when keyword is empty.
     */
    @Test
    @DisplayName("Should return all products when keyword is empty")
    void testSearchProducts_EmptyKeyword() {
        List<Product> results = productRepository.searchProducts("");

        assertThat(results).hasSize(3);
    }

    /**
     * Test searching products with keyword matching multiple fields.
     * Verifies that products matching keyword in either name or description are retrieved.
     */
    @Test
    @DisplayName("Should search products matching keyword in name or description")
    void testSearchProducts_MultipleFields() {
        List<Product> results = productRepository.searchProducts("laptop");

        assertThat(results).hasSize(2);
        assertThat(results).anyMatch(p -> p.getName().contains("Laptop"));
        assertThat(results).anyMatch(p -> p.getDescription().contains("laptop"));
    }

    /**
     * Test saving a new product.
     * Verifies that a product can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new product successfully")
    void testSaveProduct() {
        Product newProduct = new Product();
        newProduct.setProductId(UUID.randomUUID());
        newProduct.setName("Monitor");
        newProduct.setDescription("27-inch 4K monitor");
        newProduct.setPrice(BigDecimal.valueOf(399.99));

        Product saved = productRepository.save(newProduct);

        assertThat(saved).isNotNull();
        assertThat(saved.getProductId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Monitor");
    }

    /**
     * Test updating an existing product.
     * Verifies that product details can be updated.
     */
    @Test
    @DisplayName("Should update existing product")
    void testUpdateProduct() {
        testProduct1.setPrice(BigDecimal.valueOf(1199.99));
        testProduct1.setDescription("Updated description");
        Product updated = productRepository.save(testProduct1);

        assertThat(updated.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1199.99));
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getProductId()).isEqualTo(testProduct1.getProductId());
    }

    /**
     * Test finding a product by ID.
     * Verifies that a product can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find product by ID")
    void testFindById_Success() {
        Optional<Product> result = productRepository.findById(testProduct1.getProductId());

        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(testProduct1.getProductId());
        assertThat(result.get().getName()).isEqualTo("Laptop Computer");
    }

    /**
     * Test finding a product with non-existent ID.
     * Verifies that an empty Optional is returned when ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when product ID not found")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<Product> result = productRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product by ID.
     * Verifies that a product can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete product by ID")
    void testDeleteProduct() {
        UUID productId = testProduct3.getProductId();
        productRepository.deleteById(productId);
        entityManager.flush();

        Optional<Product> result = productRepository.findById(productId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all products.
     * Verifies that all products can be retrieved.
     */
    @Test
    @DisplayName("Should find all products")
    void testFindAll() {
        List<Product> allProducts = productRepository.findAll();

        assertThat(allProducts).hasSize(3);
    }

    /**
     * Test counting products.
     * Verifies that the count of products is accurate.
     */
    @Test
    @DisplayName("Should count products correctly")
    void testCount() {
        long count = productRepository.count();

        assertThat(count).isEqualTo(3L);
    }

    /**
     * Test searching products with special characters in keyword.
     * Verifies that special characters in search keyword are handled properly.
     */
    @Test
    @DisplayName("Should handle special characters in search keyword")
    void testSearchProducts_SpecialCharacters() {
        List<Product> results = productRepository.searchProducts("RGB");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("USB Keyboard");
    }
}