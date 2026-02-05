package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.ProductSearchRequest;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Product> spec = createSpecification(request);
        
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(this::mapToProductResponse);
    }

    private Specification<Product> createSpecification(ProductSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active filter
            if (request.getActiveOnly() == null || request.getActiveOnly()) {
                predicates.add(criteriaBuilder.isTrue(root.get("active")));
            }

            // Keyword search
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            // Category filter
            if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), request.getCategory()));
            }

            // Brand filter
            if (request.getBrand() != null && !request.getBrand().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), request.getBrand()));
            }

            // Price range filter
            if (request.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable createPageable(ProductSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "DESC";

        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        return PageRequest.of(page, size, sort);
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
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