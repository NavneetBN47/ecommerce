package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductInventory;
import com.ecommerce.exception.InvalidOperationException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductInventoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductInventoryRepository inventoryRepository;
    
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidOperationException("Search keyword cannot be empty");
        }
        
        List<Product> products = productRepository.searchProducts(keyword.trim());
        log.info("Found {} products matching keyword: {}", products.size(), keyword);
        
        return products.stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }
    
    @Transactional(readOnly = true)
    public Integer getAvailableQuantity(UUID productId) {
        ProductInventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product inventory", "productId", productId));
        
        return inventory.getQuantityAvailable();
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        Integer availableQty = inventoryRepository.findByProductId(product.getId())
            .map(ProductInventory::getQuantityAvailable)
            .orElse(0);
        
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .availableQty(availableQty)
            .build();
    }
}