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
 * Product Service - Business logic for product management
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

        if (keyword == null || keyword.trim().isEmpty()) {
            log.info("Empty keyword, returning all active products");
            return productRepository.findByIsActiveTrue().stream()
                    .map(this::mapToProductResponse)
                    .collect(Collectors.toList());
        }

        List<Product> products = productRepository.searchByKeyword(keyword.trim());
        log.info("Found {} products matching keyword: {}", products.size(), keyword);

        return products.stream()
                .filter(Product::getIsActive)
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
                .availableQty(product.getAvailableQty())
                .sku(product.getSku())
                .isActive(product.getIsActive())
                .build();
    }
}