package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Search keyword is null or empty");
            throw new ValidationException("Search keyword cannot be empty");
        }
        
        List<Product> products = productRepository.searchProducts(keyword.trim());
        log.info("Found {} products matching keyword: {}", products.size(), keyword);
        
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQty(product.getAvailableQty())
                .build();
    }
}