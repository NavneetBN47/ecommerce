package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service
 * Business logic for product catalog operations
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    /**
     * Search products by keyword (case-insensitive)
     * @param keyword search keyword
     * @return list of product responses
     */
    public List<ProductResponse> searchProducts(String keyword) {
        logger.info("Searching products with keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword is required");
        }

        List<Product> products = productRepository.searchByName(keyword.trim());
        logger.info("Found {} products matching keyword: {}", products.size(), keyword);

        return products.stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setAvailableQuantity(product.getAvailableQuantity());
        return response;
    }
}