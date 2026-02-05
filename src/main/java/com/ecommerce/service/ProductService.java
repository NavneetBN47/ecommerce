package com.ecommerce.service;

import com.ecommerce.dto.ProductResponseDTO;
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
 * Implements business logic for product search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Search products by keyword (case-insensitive)
     * Business Rule: Search in name and description fields
     * @param keyword search keyword (optional)
     * @return list of matching products
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);

        List<Product> products;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            // Return all active products if no keyword provided
            products = productRepository.findByIsActiveTrue();
        } else {
            // Case-insensitive search
            products = productRepository.searchByKeyword(keyword.trim());
        }

        log.info("Found {} products", products.size());

        return products.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map Product entity to ProductResponseDTO
     */
    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQty(product.getStockQuantity())
                .category(product.getCategory())
                .brand(product.getBrand())
                .sku(product.getSku())
                .build();
    }
}