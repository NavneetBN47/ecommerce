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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductTagRepository.
 * Tests repository methods for ProductTag entity operations including tag queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
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
    private ProductTag tag1;
    private ProductTag tag2;
    private ProductTag tag3;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test Product and ProductTag entities.
     */
    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        entityManager.persist(testProduct);

        // Create test tags
        tag1 = new ProductTag();
        tag1.setProduct(testProduct);
        tag1.setTag("electronics");
        entityManager.persist(tag1);

        tag2 = new ProductTag();
        tag2.setProduct(testProduct);
        tag2.setTag("bestseller");
        entityManager.persist(tag2);

        tag3 = new ProductTag();
        tag3.setProduct(testProduct);
        tag3.setTag("new-arrival");
        entityManager.persist(tag3);
        
        entityManager.flush();
    }

    /**
     * Test finding ProductTags by product ID.
     * Verifies that all tags for a product are returned.
     */
    @Test
    @DisplayName("Should find ProductTags by product ID")
    void testFindByProductId_ReturnsAllTags() {
        // When
        List<ProductTag> result = productTagRepository.findByProductId(testProduct.getProductId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(ProductTag::getTag)
            .containsExactlyInAnyOrder("electronics", "bestseller", "new-arrival");
    }

    /**
     * Test finding ProductTags by product ID when no tags exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when product has no tags")
    void testFindByProductId_WhenNoTags_ReturnsEmpty() {
        // Given
        Product productWithoutTags = new Product();
        productWithoutTags.setProductId(UUID.randomUUID());
        productWithoutTags.setName("Product Without Tags");
        productWithoutTags.setPrice(49.99);
        entityManager.persist(productWithoutTags);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByProductId(productWithoutTags.getProductId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding ProductTags by tag name.
     * Verifies that all products with the specified tag are returned.
     */
    @Test
    @DisplayName("Should find ProductTags by tag name")
    void testFindByTag_ReturnsMatchingTags() {
        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTag()).isEqualToIgnoringCase("electronics");
    }

    /**
     * Test finding ProductTags by tag name with case insensitivity.
     * Verifies that tag search is case-insensitive.
     */
    @Test
    @DisplayName("Should find ProductTags by tag name case-insensitively")
    void testFindByTag_CaseInsensitive_ReturnsMatchingTags() {
        // When
        List<ProductTag> resultLower = productTagRepository.findByTag("electronics");
        List<ProductTag> resultUpper = productTagRepository.findByTag("ELECTRONICS");
        List<ProductTag> resultMixed = productTagRepository.findByTag("ElEcTrOnIcS");

        // Then
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
        assertThat(resultLower.get(0).getId()).isEqualTo(resultUpper.get(0).getId());
    }

    /**
     * Test finding ProductTags by non-existent tag.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when tag does not exist")
    void testFindByTag_WhenNotExists_ReturnsEmpty() {
        // When
        List<ProductTag> result = productTagRepository.findByTag("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting ProductTags by product ID.
     * Verifies that all tags for a product are removed.
     */
    @Test
    @DisplayName("Should delete ProductTags by product ID")
    void testDeleteByProductProductId_Success() {
        // When
        productTagRepository.deleteByProductProductId(testProduct.getProductId());
        entityManager.flush();

        // Then
        List<ProductTag> result = productTagRepository.findByProductId(testProduct.getProductId());
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting ProductTags by product ID when no tags exist.
     * Verifies that no exception is thrown.
     */
    @Test
    @DisplayName("Should handle delete by product ID when no tags exist")
    void testDeleteByProductProductId_WhenNoTags_NoException() {
        // Given
        Product productWithoutTags = new Product();
        productWithoutTags.setProductId(UUID.randomUUID());
        productWithoutTags.setName("Product Without Tags");
        productWithoutTags.setPrice(49.99);
        entityManager.persist(productWithoutTags);
        entityManager.flush();

        // When & Then - should not throw exception
        productTagRepository.deleteByProductProductId(productWithoutTags.getProductId());
        entityManager.flush();
    }

    /**
     * Test saving a new ProductTag.
     * Verifies that the ProductTag is persisted correctly.
     */
    @Test
    @DisplayName("Should save new ProductTag successfully")
    void testSave_NewTag_Success() {
        // Given
        ProductTag newTag = new ProductTag();
        newTag.setProduct(testProduct);
        newTag.setTag("featured");

        // When
        ProductTag savedTag = productTagRepository.save(newTag);

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getTag()).isEqualTo("featured");
        assertThat(savedTag.getProduct().getProductId()).isEqualTo(testProduct.getProductId());
    }

    /**
     * Test updating an existing ProductTag.
     * Verifies that ProductTag modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing ProductTag successfully")
    void testSave_UpdateTag_Success() {
        // Given
        tag1.setTag("updated-electronics");

        // When
        ProductTag updatedTag = productTagRepository.save(tag1);
        entityManager.flush();

        // Then
        assertThat(updatedTag.getTag()).isEqualTo("updated-electronics");
        assertThat(updatedTag.getId()).isEqualTo(tag1.getId());
    }

    /**
     * Test deleting a ProductTag by ID.
     * Verifies that the ProductTag is removed from the database.
     */
    @Test
    @DisplayName("Should delete ProductTag by ID successfully")
    void testDeleteById_Success() {
        // Given
        Long tagId = tag1.getId();

        // When
        productTagRepository.deleteById(tagId);
        entityManager.flush();

        // Then
        List<ProductTag> remainingTags = productTagRepository.findByProductId(testProduct.getProductId());
        assertThat(remainingTags).hasSize(2);
        assertThat(remainingTags).extracting(ProductTag::getTag)
            .doesNotContain("electronics");
    }

    /**
     * Test finding all ProductTags.
     * Verifies that all persisted ProductTags are retrieved.
     */
    @Test
    @DisplayName("Should find all ProductTags")
    void testFindAll_ReturnsAllTags() {
        // When
        List<ProductTag> allTags = productTagRepository.findAll();

        // Then
        assertThat(allTags).isNotEmpty();
        assertThat(allTags).hasSize(3);
    }

    /**
     * Test that multiple products can have the same tag.
     * Verifies tag reusability across products.
     */
    @Test
    @DisplayName("Should allow multiple products to have the same tag")
    void testMultipleProductsSameTag() {
        // Given
        Product anotherProduct = new Product();
        anotherProduct.setProductId(UUID.randomUUID());
        anotherProduct.setName("Another Product");
        anotherProduct.setPrice(149.99);
        entityManager.persist(anotherProduct);

        ProductTag anotherTag = new ProductTag();
        anotherTag.setProduct(anotherProduct);
        anotherTag.setTag("electronics");
        entityManager.persist(anotherTag);
        entityManager.flush();

        // When
        List<ProductTag> electronicsTag = productTagRepository.findByTag("electronics");

        // Then
        assertThat(electronicsTag).hasSize(2);
        assertThat(electronicsTag).extracting(pt -> pt.getProduct().getProductId())
            .containsExactlyInAnyOrder(testProduct.getProductId(), anotherProduct.getProductId());
    }

    /**
     * Test that ProductTag maintains relationship with Product.
     * Verifies bidirectional relationship integrity.
     */
    @Test
    @DisplayName("Should maintain relationship with Product")
    void testProductTagProductRelationship() {
        // When
        List<ProductTag> tags = productTagRepository.findByProductId(testProduct.getProductId());

        // Then
        assertThat(tags).allMatch(tag -> tag.getProduct().getProductId().equals(testProduct.getProductId()));
    }
}