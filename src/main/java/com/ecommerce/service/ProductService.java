package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Product operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new product
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());
        
        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new ResourceAlreadyExistsException("Product with SKU already exists: " + productDTO.getSku());
        }
        
        Product product = modelMapper.map(productDTO, Product.class);
        product.setActive(true);
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully: {}", savedProduct.getId());
        
        return modelMapper.map(savedProduct, ProductDTO.class);
    }
    
    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product by id: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        return modelMapper.map(product, ProductDTO.class);
    }
    
    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        log.info("Fetching all active products");
        
        return productRepository.findByActiveTrue().stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        
        return productRepository.findAll().stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        
        return productRepository.searchByNameOrCategory(searchTerm).stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        
        return productRepository.findByCategoryAndActiveTrue(category).stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Update product
     */
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Check if new SKU already exists (if changed)
        if (!product.getSku().equals(productDTO.getSku()) && 
            productRepository.existsBySku(productDTO.getSku())) {
            throw new ResourceAlreadyExistsException("Product with SKU already exists: " + productDTO.getSku());
        }
        
        product.setSku(productDTO.getSku());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategory(productDTO.getCategory());
        product.setImageUrl(productDTO.getImageUrl());
        product.setActive(productDTO.getActive());
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", updatedProduct.getId());
        
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }
    
    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        productRepository.delete(product);
        log.info("Product deleted successfully: {}", id);
    }
    
    /**
     * Update product stock
     */
    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for product: {}, quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        
        log.info("Stock updated successfully for product: {}", productId);
    }
}