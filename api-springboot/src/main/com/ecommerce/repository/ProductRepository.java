package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0")
    Page<Product> findAllAvailable(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND p.category.categoryId = :categoryId " +
           "AND (:inStock = false OR p.stockQuantity > 0)")
    Page<Product> findByCategory(@Param("categoryId") Long categoryId, 
                                  @Param("inStock") Boolean inStock, 
                                  Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND p.price BETWEEN :minPrice AND :maxPrice " +
           "AND (:inStock = false OR p.stockQuantity > 0)")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    @Param("inStock") Boolean inStock,
                                    Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.isActive = true")
    Optional<Product> findActiveProduct(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity " +
           "WHERE p.productId = :productId AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT CASE WHEN p.stockQuantity >= :quantity THEN true ELSE false END " +
           "FROM Product p WHERE p.productId = :productId")
    boolean hasEnoughStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}