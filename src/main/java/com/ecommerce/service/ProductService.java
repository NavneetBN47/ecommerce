package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ProductMapper;
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
 * Service class for Product operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    /**
     * Create a new product
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new ResourceAlreadyExistsException("Product with SKU already exists: " + productDTO.getSku());
        }

        // Map DTO to entity
        Product product = productMapper.toEntity(productDTO);

        // Set category if provided
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        // Set default values
        product.setActive(true);

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully: {}", savedProduct.getName());

        return productMapper.toDTO(savedProduct);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return productMapper.toDTO(product);
    }

    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toDTO(product);
    }

    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        log.debug("Fetching all active products");
        return productRepository.findByActiveTrue().stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProductsByName(String searchTerm, Pageable pageable) {
        log.debug("Searching products by name: {}", searchTerm);
        return productRepository.searchByName(searchTerm, pageable)
            .map(productMapper::toDTO);
    }

    /**
     * Search products by name or description (case-insensitive)
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products: {}", searchTerm);
        return productRepository.searchByNameOrDescription(searchTerm, pageable)
            .map(productMapper::toDTO);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.debug("Fetching products by category ID: {}", categoryId);
        return productRepository.findByCategoryId(categoryId).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get products by category with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products by category ID with pagination: {}", categoryId);
        return productRepository.findByCategoryId(categoryId, pageable)
            .map(productMapper::toDTO);
    }

    /**
     * Update product
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Check if SKU is being changed and if it already exists
        if (productDTO.getSku() != null && !productDTO.getSku().equals(existingProduct.getSku())) {
            if (productRepository.existsBySku(productDTO.getSku())) {
                throw new ResourceAlreadyExistsException("Product with SKU already exists: " + productDTO.getSku());
            }
            existingProduct.setSku(productDTO.getSku());
        }

        // Update fields
        if (productDTO.getName() != null) {
            existingProduct.setName(productDTO.getName());
        }

        if (productDTO.getDescription() != null) {
            existingProduct.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() != null) {
            existingProduct.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }

        if (productDTO.getImageUrl() != null) {
            existingProduct.setImageUrl(productDTO.getImageUrl());
        }

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", updatedProduct.getName());

        return productMapper.toDTO(updatedProduct);
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Update product stock
     */
    public ProductDTO updateStock(Long id, Integer quantity) {
        log.info("Updating stock for product ID: {} to quantity: {}", id, quantity);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        log.info("Product stock updated successfully: {}", updatedProduct.getName());

        return productMapper.toDTO(updatedProduct);
    }
}