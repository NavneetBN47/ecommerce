package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for product catalog operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Search products by keyword (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);
        
        List<Product> products;
        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRepository.findByIsActiveTrue();
        } else {
            products = productRepository.searchProducts(keyword.trim());
        }
        
        log.info("Found {} products", products.size());
        
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQty(product.getStockQuantity())
                .build();
    }
}