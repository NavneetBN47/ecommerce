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
 * Service class for Product entity operations
 * Implements case-insensitive product search
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
        if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new ResourceAlreadyExistsException("Product with SKU already exists: " + productDTO.getSku());
        }

        Product product = mapToEntity(productDTO);
        product = productRepository.save(product);
        log.info("Product created successfully: {}", product.getName());

        return mapToDTO(product);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToDTO(product);
    }

    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        log.debug("Fetching all active products");
        return productRepository.findByActiveTrue().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        return productRepository.searchByNameOrDescriptionIgnoreCase(searchTerm, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get products by category (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategoryIgnoreCase(category).stream()
            .map(this::mapToDTO)
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

        product = productRepository.save(product);
        log.info("Product updated successfully: {}", product.getName());

        return mapToDTO(product);
    }

    /**
     * Delete product (soft delete)
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product deleted successfully: {}", product.getName());
    }

    /**
     * Check and reduce stock
     */
    public void reduceStock(Long productId, Integer quantity) {
        log.debug("Reducing stock for product: {} by quantity: {}", productId, quantity);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), quantity));
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        log.debug("Stock reduced successfully for product: {}", productId);
    }

    /**
     * Map Product entity to ProductDTO
     */
    private ProductDTO mapToDTO(Product product) {
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

    /**
     * Map ProductDTO to Product entity
     */
    private Product mapToEntity(ProductDTO dto) {
        return Product.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .sku(dto.getSku())
            .price(dto.getPrice())
            .stockQuantity(dto.getStockQuantity())
            .category(dto.getCategory())
            .imageUrl(dto.getImageUrl())
            .active(dto.getActive() != null ? dto.getActive() : true)
            .build();
    }
}