package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service - Handles product-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Create a new product
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDTO.getSku());
        }

        Product product = Product.builder()
            .name(productDTO.getName())
            .description(productDTO.getDescription())
            .sku(productDTO.getSku())
            .price(productDTO.getPrice())
            .stockQuantity(productDTO.getStockQuantity())
            .category(productDTO.getCategory())
            .imageUrl(productDTO.getImageUrl())
            .isActive(true)
            .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return convertToDTO(savedProduct);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDTO(product);
    }

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
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        return productRepository.searchByNameOrCategory(searchTerm).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        return productRepository.findByCategoryAndIsActiveTrue(category).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update product
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Update fields
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }

        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStockQuantity() != null) {
            product.setStockQuantity(productDTO.getStockQuantity());
        }

        if (productDTO.getCategory() != null) {
            product.setCategory(productDTO.getCategory());
        }

        if (productDTO.getImageUrl() != null) {
            product.setImageUrl(productDTO.getImageUrl());
        }

        if (productDTO.getIsActive() != null) {
            product.setIsActive(productDTO.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", updatedProduct.getId());

        return convertToDTO(updatedProduct);
    }

    /**
     * Delete product (soft delete)
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product deleted successfully: {}", id);
    }

    /**
     * Convert Product entity to DTO
     */
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
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