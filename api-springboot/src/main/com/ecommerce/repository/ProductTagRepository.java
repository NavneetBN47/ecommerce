package com.ecommerce.repository;

import com.ecommerce.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {

    @Query("SELECT pt FROM ProductTag pt WHERE pt.product.productId = :productId")
    List<ProductTag> findByProductId(@Param("productId") Long productId);

    @Query("SELECT pt FROM ProductTag pt WHERE LOWER(pt.tag) = LOWER(:tag)")
    List<ProductTag> findByTag(@Param("tag") String tag);

    void deleteByProductProductId(Long productId);
}