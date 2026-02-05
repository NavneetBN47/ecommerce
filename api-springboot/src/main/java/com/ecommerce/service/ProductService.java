package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Product operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new product
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getProductName());

        if (productDTO.getSku() != null && productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDTO.getSku());
        }

        Category category = null;
        if (productDTO.getCategoryId() != null) {
            category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
        }

        Product product = Product.builder()
            .productName(productDTO.getProductName())
            .description(productDTO.getDescription())
            .sku(productDTO.getSku())
            .price(productDTO.getPrice())
            .stockQuantity(productDTO.getStockQuantity())
            .category(category)
            .imageUrl(productDTO.getImageUrl())
            .isActive(true)
            .build();

        product = productRepository.save(product);
        log.info("Product created successfully: {}", product.getProductId());

        return mapToDTO(product);
    }

    /**
     * Get all active products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByIsActive(true, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToDTO(product);
    }

    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.info("Searching products with term: {}", searchTerm);
        return productRepository.searchByProductName(searchTerm, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryCategoryIdAndIsActive(categoryId, true, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Update product
     */
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        log.info("Updating product: {}", productId);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (productDTO.getSku() != null && !product.getSku().equals(productDTO.getSku()) &&
            productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDTO.getSku());
        }

        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setSku(productDTO.getSku());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product updated successfully: {}", productId);

        return mapToDTO(product);
    }

    /**
     * Delete product (soft delete)
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product: {}", productId);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product deleted successfully: {}", productId);
    }

    /**
     * Get product entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public Product getProductEntityById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    /**
     * Map Product entity to ProductDTO
     */
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .description(product.getDescription())
            .sku(product.getSku())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
            .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
            .imageUrl(product.getImageUrl())
            .isActive(product.getIsActive())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}