package com.ecommerce.repository;

import com.ecommerce.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, UUID> {
    
    Optional<ProductInventory> findByProductId(UUID productId);
}