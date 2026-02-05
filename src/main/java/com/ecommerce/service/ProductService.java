package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.CategoryRepository;
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
 * Service layer for Product operations
 * Implements case-insensitive product search as per business requirements
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return mapToDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all active products");
        return productRepository.findByActiveTrue(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Case-insensitive search for products by name
     * Implements business requirement for case-insensitive product search
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term (case-insensitive): {}", searchTerm);
        return productRepository.searchProductsIgnoreCase(searchTerm, pageable)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products for category ID: {}", categoryId);
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getFeaturedProducts(Pageable pageable) {
        log.debug("Fetching featured products");
        return productRepository.findByFeaturedTrueAndActiveTrue(pageable)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getInStockProducts(Pageable pageable) {
        log.debug("Fetching in-stock products");
        return productRepository.findInStockProducts(pageable)
            .map(this::mapToDTO);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with SKU: {}", productDTO.getSku());
        
        // Check for duplicate SKU
        if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new DuplicateResourceException("Product", "sku", productDTO.getSku());
        }

        Product product = Product.builder()
            .sku(productDTO.getSku())
            .name(productDTO.getName())
            .description(productDTO.getDescription())
            .price(productDTO.getPrice())
            .discountPrice(productDTO.getDiscountPrice())
            .stockQuantity(productDTO.getStockQuantity())
            .imageUrl(productDTO.getImageUrl())
            .active(productDTO.getActive() != null ? productDTO.getActive() : true)
            .featured(productDTO.getFeatured() != null ? productDTO.getFeatured() : false)
            .brand(productDTO.getBrand())
            .build();

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return mapToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setDiscountPrice(productDTO.getDiscountPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setActive(productDTO.getActive());
        product.setFeatured(productDTO.getFeatured());
        product.setBrand(productDTO.getBrand());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        
        return mapToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        
        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Check and reserve stock for product
     * Implements quantity check business rule
     */
    @Transactional
    public void reserveStock(Long productId, int quantity) {
        log.debug("Reserving {} units of product ID: {}", quantity, productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), quantity));
        }
        
        product.decreaseStock(quantity);
        productRepository.save(product);
        log.info("Stock reserved successfully for product ID: {}", productId);
    }

    @Transactional
    public void releaseStock(Long productId, int quantity) {
        log.debug("Releasing {} units of product ID: {}", quantity, productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        product.increaseStock(quantity);
        productRepository.save(product);
        log.info("Stock released successfully for product ID: {}", productId);
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .discountPrice(product.getDiscountPrice())
            .stockQuantity(product.getStockQuantity())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .imageUrl(product.getImageUrl())
            .active(product.getActive())
            .featured(product.getFeatured())
            .brand(product.getBrand())
            .rating(product.getRating())
            .reviewCount(product.getReviewCount())
            .effectivePrice(product.getEffectivePrice())
            .inStock(product.isInStock())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}