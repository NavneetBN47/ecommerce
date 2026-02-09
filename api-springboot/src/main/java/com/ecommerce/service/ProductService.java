package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for product catalog operations
 * Implements LLD product search business logic
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    /**
     * Search products by keyword (case-insensitive)
     * Implements LLD GET /api/products/search?keyword=...
     */
    public List<ProductResponse> searchProducts(String keyword) {
        logger.info("Searching products with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }

        List<Product> products = productRepository.searchActiveByKeyword(keyword.trim());
        logger.info("Found {} products matching keyword: {}", products.size(), keyword);

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID (internal use)
     */
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        Integer availableQty = 0;
        if (product.getInventory() != null) {
            availableQty = product.getInventory().getQuantityAvailable();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQty(availableQty)
                .build();
    }
}