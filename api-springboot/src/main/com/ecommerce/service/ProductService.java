package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
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
 * Product Service
 * Handles business logic for product operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        
        List<Product> products = productRepository.searchByProductName(searchTerm);
        
        log.info("Found {} products matching search term: {}", products.size(), searchTerm);
        
        return products.stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        
        List<Product> products = productRepository.findAll();
        
        log.info("Retrieved {} products", products.size());
        
        return products.stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long productId) {
        log.info("Fetching product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                log.error("Product not found: {}", productId);
                return new ResourceNotFoundException("Product not found with ID: " + productId);
            });
        
        return modelMapper.map(product, ProductDTO.class);
    }

    /**
     * Get available products (quantity > 0)
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAvailableProducts() {
        log.info("Fetching available products");
        
        List<Product> products = productRepository.findByAvailableQtyGreaterThan(0);
        
        log.info("Found {} available products", products.size());
        
        return products.stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
            .collect(Collectors.toList());
    }
}