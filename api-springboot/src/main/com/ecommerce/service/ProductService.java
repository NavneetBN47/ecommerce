package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for product management
 * Implements case-insensitive product search and stock validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all active products");
        return productRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long productId) {
        log.info("Fetching product with ID: {}", productId);
        Product product = productRepository.findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        return convertToDTO(product);
    }

    /**
     * Search products with case-insensitive search
     * Business Rule: Product search must be case-insensitive
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }

        return productRepository.searchProducts(searchTerm.trim()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Fetching products in category: {}", category);
        return productRepository.findByCategoryIgnoreCaseAndIsActiveTrue(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products in stock
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsInStock() {
        log.info("Fetching products in stock");
        return productRepository.findProductsInStock().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validate product stock
     * Business Rule: Always check stock before cart operations
     */
    @Transactional(readOnly = true)
    public boolean hasStock(Long productId, int quantity) {
        return productRepository.hasStock(productId, quantity);
    }

    /**
     * Get product entity (internal use)
     */
    @Transactional(readOnly = true)
    public Product getProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    /**
     * Convert Product entity to DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        return dto;
    }
}