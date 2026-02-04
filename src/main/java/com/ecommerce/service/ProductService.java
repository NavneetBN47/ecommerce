package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String search, Integer page, Integer size) {
        log.info("Searching products with query: {}", search);
        
        if (search == null || search.trim().isEmpty()) {
            Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
            Page<Product> products = productRepository.findByIsActiveTrue(pageable);
            return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
        }
        
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productRepository.searchActiveProducts(search, pageable);
            return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
        }
        
        List<Product> products = productRepository.searchByName(search);
        return products.stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        log.info("Fetching product: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        
        return mapToProductResponse(product);
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .description(product.getDescription())
            .price(product.getPrice())
            .availableQty(product.getAvailableQty())
            .isActive(product.getIsActive())
            .build();
    }
}
