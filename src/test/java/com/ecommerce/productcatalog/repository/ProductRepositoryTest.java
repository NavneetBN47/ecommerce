package com.ecommerce.productcatalog.repository;

import com.ecommerce.productcatalog.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Electronics");
        product.setStockQuantity(100);
    }

    @Test
    @DisplayName("Find By Name Containing - Success")
    void testFindByNameContaining_Success() {
        entityManager.persist(product);
        entityManager.flush();

        List<Product> found = productRepository.findByNameContainingIgnoreCase("Test");

        assertFalse(found.isEmpty());
        assertEquals("Test Product", found.get(0).getName());
    }

    @Test
    @DisplayName("Find By Category - Success")
    void testFindByCategory_Success() {
        entityManager.persist(product);
        entityManager.flush();

        List<Product> found = productRepository.findByCategory("Electronics");

        assertFalse(found.isEmpty());
        assertEquals("Electronics", found.get(0).getCategory());
    }

    @Test
    @DisplayName("Find By Category - Empty Result")
    void testFindByCategory_Empty() {
        List<Product> found = productRepository.findByCategory("NonExistent");

        assertTrue(found.isEmpty());
    }
}
