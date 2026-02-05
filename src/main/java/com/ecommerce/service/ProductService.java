package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Product operations
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
        
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new ResourceAlreadyExistsException("Product with SKU already exists: " + productDTO.getSku());
        }
        
        Product product = Product.builder()
            .name(productDTO.getName())
            .description(productDTO.getDescription())
            .sku(productDTO.getSku())
            .price(productDTO.getPrice())
            .stockQuantity(productDTO.getStockQuantity())
            .category(productDTO.getCategory())
            .imageUrl(productDTO.getImageUrl())
            .active(true)
            .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully: {}", savedProduct.getId());
        
        return convertToDTO(savedProduct);
    }
    
    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDTO(product);
    }
    
    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        log.debug("Fetching all active products");
        return productRepository.findByActiveTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        return productRepository.searchByNameOrDescriptionIgnoreCase(searchTerm, pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategoryAndActiveTrue(category).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update product
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
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
        if (productDTO.getActive() != null) {
            product.setActive(productDTO.getActive());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", updatedProduct.getId());
        
        return convertToDTO(updatedProduct);
    }
    
    /**
     * Delete product
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        
        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);
    }
    
    /**
     * Check and reserve stock for a product
     */
    public void reserveStock(Long productId, Integer quantity) {
        log.debug("Reserving stock for product: {}, quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), quantity));
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        
        log.info("Stock reserved successfully for product: {}", productId);
    }
    
    /**
     * Release reserved stock
     */
    public void releaseStock(Long productId, Integer quantity) {
        log.debug("Releasing stock for product: {}, quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
        
        log.info("Stock released successfully for product: {}", productId);
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
            .active(product.getActive())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}