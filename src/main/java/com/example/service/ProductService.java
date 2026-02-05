package com.example.service;

import com.example.dto.ProductDTO;
import com.example.entity.Product;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for Product operations
 * Implements case-insensitive product search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long productId) {
        log.info("Fetching product with id: {}", productId);
        
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        return convertToDTO(product);
    }

    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");
        
        return productRepository.findByIsDeletedFalse(pageable)
            .map(this::convertToDTO);
    }

    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProductsByName(String searchTerm, Pageable pageable) {
        log.info("Searching products by name: {} (case-insensitive)", searchTerm);
        
        return productRepository.searchByNameCaseInsensitive(searchTerm, pageable)
            .map(this::convertToDTO);
    }

    /**
     * Search products across multiple fields (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.info("Searching products: {} (case-insensitive)", searchTerm);
        
        return productRepository.searchProducts(searchTerm, pageable)
            .map(this::convertToDTO);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        log.info("Fetching products by category: {}", category);
        
        return productRepository.findByCategoryAndIsDeletedFalse(category, pageable)
            .map(this::convertToDTO);
    }

    /**
     * Convert Product entity to DTO
     */
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .category(product.getCategory())
            .imageUrl(product.getImageUrl())
            .isActive(product.getIsActive())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}