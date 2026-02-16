package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for ProductTagRepository.
 * Tests all repository methods including custom query methods for product tag operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductTagRepository Tests")
public class test_ProductTagRepository {

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;
    private ProductTag testTag1;
    private ProductTag testTag2;
    private Long productId;

    /**
     * Set up test data before each test method execution.
     * Creates test product and product tag entities.
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);
        entityManager.flush();
        
        productId = testProduct.getProductId();

        testTag1 = new ProductTag();
        testTag1.setProduct(testProduct);
        testTag1.setTag("electronics");
        entityManager.persist(testTag1);

        testTag2 = new ProductTag();
        testTag2.setProduct(testProduct);
        testTag2.setTag("gadgets");
        entityManager.persist(testTag2);

        entityManager.flush();
    }

    /**
     * Test finding product tags by product ID.
     * Verifies that all tags for a given product are retrieved.
     */
    @Test
    @DisplayName("Should find product tags by product ID")
    void testFindByProductId_Success() {
        List<ProductTag> results = productTagRepository.findByProductId(productId);

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ProductTag::getTag)
            .containsExactlyInAnyOrder("electronics", "gadgets");
    }

    /**
     * Test finding product tags with non-existent product ID.
     * Verifies that an empty list is returned when product ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty list when product ID not found")
    void testFindByProductId_NotFound() {
        List<ProductTag> results = productTagRepository.findByProductId(999L);

        assertThat(results).isEmpty();
    }

    /**
     * Test finding product tags by tag name.
     * Verifies that all products with a specific tag are retrieved.
     */
    @Test
    @DisplayName("Should find product tags by tag name")
    void testFindByTag_Success() {
        List<ProductTag> results = productTagRepository.findByTag("electronics");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTag()).isEqualToIgnoringCase("electronics");
    }

    /**
     * Test finding product tags by tag name case-insensitively.
     * Verifies that tag search is case-insensitive.
     */
    @Test
    @DisplayName("Should find product tags by tag name case-insensitively")
    void testFindByTag_CaseInsensitive() {
        List<ProductTag> resultsLower = productTagRepository.findByTag("electronics");
        List<ProductTag> resultsUpper = productTagRepository.findByTag("ELECTRONICS");
        List<ProductTag> resultsMixed = productTagRepository.findByTag("ElEcTrOnIcS");

        assertThat(resultsLower).hasSize(1);
        assertThat(resultsUpper).hasSize(1);
        assertThat(resultsMixed).hasSize(1);
    }

    /**
     * Test finding product tags with non-existent tag.
     * Verifies that an empty list is returned when tag doesn't exist.
     */
    @Test
    @DisplayName("Should return empty list when tag not found")
    void testFindByTag_NotFound() {
        List<ProductTag> results = productTagRepository.findByTag("nonexistent");

        assertThat(results).isEmpty();
    }

    /**
     * Test finding product tags with multiple products having same tag.
     * Verifies that all products with the same tag are retrieved.
     */
    @Test
    @DisplayName("Should find multiple products with same tag")
    void testFindByTag_MultipleProducts() {
        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Another Product");
        product2.setPrice(BigDecimal.valueOf(49.99));
        entityManager.persist(product2);

        ProductTag tag3 = new ProductTag();
        tag3.setProduct(product2);
        tag3.setTag("electronics");
        entityManager.persist(tag3);
        entityManager.flush();

        List<ProductTag> results = productTagRepository.findByTag("electronics");

        assertThat(results).hasSize(2);
    }

    /**
     * Test deleting product tags by product ID.
     * Verifies that all tags for a product are successfully deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete product tags by product ID")
    void testDeleteByProductProductId() {
        productTagRepository.deleteByProductProductId(productId);
        entityManager.flush();

        List<ProductTag> results = productTagRepository.findByProductId(productId);
        assertThat(results).isEmpty();
    }

    /**
     * Test deleting product tags with non-existent product ID.
     * Verifies that delete operation handles non-existent product gracefully.
     */
    @Test
    @Transactional
    @DisplayName("Should handle delete with non-existent product ID")
    void testDeleteByProductProductId_NotFound() {
        long countBefore = productTagRepository.count();

        productTagRepository.deleteByProductProductId(999L);
        entityManager.flush();

        long countAfter = productTagRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Test saving a new product tag.
     * Verifies that a product tag can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new product tag successfully")
    void testSaveProductTag() {
        ProductTag newTag = new ProductTag();
        newTag.setProduct(testProduct);
        newTag.setTag("new-tag");

        ProductTag saved = productTagRepository.save(newTag);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTag()).isEqualTo("new-tag");
    }

    /**
     * Test updating an existing product tag.
     * Verifies that product tag can be updated.
     */
    @Test
    @DisplayName("Should update existing product tag")
    void testUpdateProductTag() {
        testTag1.setTag("updated-electronics");
        ProductTag updated = productTagRepository.save(testTag1);

        assertThat(updated.getTag()).isEqualTo("updated-electronics");
        assertThat(updated.getId()).isEqualTo(testTag1.getId());
    }

    /**
     * Test deleting a product tag by ID.
     * Verifies that a product tag can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete product tag by ID")
    void testDeleteProductTag() {
        Long tagId = testTag1.getId();
        productTagRepository.deleteById(tagId);
        entityManager.flush();

        assertThat(productTagRepository.findById(tagId)).isEmpty();
    }

    /**
     * Test finding all product tags.
     * Verifies that all product tags can be retrieved.
     */
    @Test
    @DisplayName("Should find all product tags")
    void testFindAll() {
        List<ProductTag> allTags = productTagRepository.findAll();

        assertThat(allTags).hasSize(2);
    }

    /**
     * Test counting product tags.
     * Verifies that the count of product tags is accurate.
     */
    @Test
    @DisplayName("Should count product tags correctly")
    void testCount() {
        long count = productTagRepository.count();

        assertThat(count).isEqualTo(2L);
    }

    /**
     * Test saving product tag with empty tag name.
     * Verifies that product tags with empty names can be persisted.
     */
    @Test
    @DisplayName("Should handle product tag with empty tag name")
    void testSaveProductTag_EmptyTag() {
        ProductTag emptyTag = new ProductTag();
        emptyTag.setProduct(testProduct);
        emptyTag.setTag("");

        ProductTag saved = productTagRepository.save(emptyTag);

        assertThat(saved.getTag()).isEmpty();
    }
}