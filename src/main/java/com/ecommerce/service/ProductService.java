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
 * Product Service with case-insensitive search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getActiveProducts() {
        log.info("Fetching active products");
        return productRepository.findByActiveTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product by id: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        log.info("Fetching product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return convertToDTO(product);
    }

    /**
     * Case-insensitive product search
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String search) {
        log.info("Searching products with term: {}", search);
        return productRepository.searchProducts(search).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());
        
        if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDTO.getSku());
        }

        Product product = Product.builder()
            .name(productDTO.getName())
            .description(productDTO.getDescription())
            .sku(productDTO.getSku())
            .price(productDTO.getPrice())
            .stockQuantity(productDTO.getStockQuantity())
            .category(productDTO.getCategory())
            .brand(productDTO.getBrand())
            .imageUrl(productDTO.getImageUrl())
            .active(true)
            .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (productDTO.getName() != null) product.setName(productDTO.getName());
        if (productDTO.getDescription() != null) product.setDescription(productDTO.getDescription());
        if (productDTO.getPrice() != null) product.setPrice(productDTO.getPrice());
        if (productDTO.getStockQuantity() != null) product.setStockQuantity(productDTO.getStockQuantity());
        if (productDTO.getCategory() != null) product.setCategory(productDTO.getCategory());
        if (productDTO.getBrand() != null) product.setBrand(productDTO.getBrand());
        if (productDTO.getImageUrl() != null) product.setImageUrl(productDTO.getImageUrl());
        if (productDTO.getActive() != null) product.setActive(productDTO.getActive());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", id);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        
        productRepository.deleteById(id);
        log.info("Product deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.info("Fetching all product categories");
        return productRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        log.info("Fetching all product brands");
        return productRepository.findAllBrands();
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .category(product.getCategory())
            .brand(product.getBrand())
            .imageUrl(product.getImageUrl())
            .active(product.getActive())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}