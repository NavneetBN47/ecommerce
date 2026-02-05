package com.example.service;

import com.example.dto.ProductDTO;
import com.example.entity.Product;
import com.example.exception.DuplicateResourceException;
import com.example.exception.InsufficientStockException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Product entity operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Create a new product.
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());
        
        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDTO.getSku());
        }
        
        Product product = convertToEntity(productDTO);
        product = productRepository.save(product);
        
        log.info("Product created successfully: {}", product.getId());
        return convertToDTO(product);
    }
    
    /**
     * Get product by ID.
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDTO(product);
    }
    
    /**
     * Get all active products with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllActiveProducts(Pageable pageable) {
        log.debug("Fetching all active products");
        return productRepository.findAllActiveProducts(pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Search products by name (case-insensitive).
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        return productRepository.advancedSearch(searchTerm, pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Get products by category.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category, pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Get featured products.
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getFeaturedProducts() {
        log.debug("Fetching featured products");
        return productRepository.findFeaturedProducts().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update product.
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product: {}", id);
        
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
        if (productDTO.getDiscountPrice() != null) {
            product.setDiscountPrice(productDTO.getDiscountPrice());
        }
        if (productDTO.getStockQuantity() != null) {
            product.setStockQuantity(productDTO.getStockQuantity());
        }
        if (productDTO.getCategory() != null) {
            product.setCategory(productDTO.getCategory());
        }
        if (productDTO.getBrand() != null) {
            product.setBrand(productDTO.getBrand());
        }
        if (productDTO.getImageUrl() != null) {
            product.setImageUrl(productDTO.getImageUrl());
        }
        if (productDTO.getActive() != null) {
            product.setActive(productDTO.getActive());
        }
        if (productDTO.getFeatured() != null) {
            product.setFeatured(productDTO.getFeatured());
        }
        
        product = productRepository.save(product);
        log.info("Product updated successfully: {}", id);
        
        return convertToDTO(product);
    }
    
    /**
     * Delete product (soft delete).
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        product.setActive(false);
        productRepository.save(product);
        
        log.info("Product deleted successfully: {}", id);
    }
    
    /**
     * Check and reserve stock for product.
     */
    public void reserveStock(Long productId, Integer quantity) {
        log.debug("Reserving stock for product: {} quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (!product.hasSufficientStock(quantity)) {
            throw new InsufficientStockException(
                "Insufficient stock for product: " + product.getName() + 
                ". Available: " + product.getStockQuantity() + ", Requested: " + quantity
            );
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        
        log.debug("Stock reserved successfully for product: {}", productId);
    }
    
    /**
     * Convert Product entity to DTO.
     */
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .price(product.getPrice())
            .discountPrice(product.getDiscountPrice())
            .effectivePrice(product.getEffectivePrice())
            .stockQuantity(product.getStockQuantity())
            .category(product.getCategory())
            .brand(product.getBrand())
            .imageUrl(product.getImageUrl())
            .active(product.getActive())
            .featured(product.getFeatured())
            .inStock(product.isInStock())
            .rating(product.getRating())
            .reviewCount(product.getReviewCount())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
    
    /**
     * Convert ProductDTO to entity.
     */
    private Product convertToEntity(ProductDTO dto) {
        return Product.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .sku(dto.getSku())
            .price(dto.getPrice())
            .discountPrice(dto.getDiscountPrice())
            .stockQuantity(dto.getStockQuantity())
            .category(dto.getCategory())
            .brand(dto.getBrand())
            .imageUrl(dto.getImageUrl())
            .active(dto.getActive() != null ? dto.getActive() : true)
            .featured(dto.getFeatured() != null ? dto.getFeatured() : false)
            .build();
    }
}