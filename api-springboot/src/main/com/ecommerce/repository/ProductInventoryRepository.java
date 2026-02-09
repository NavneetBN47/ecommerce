package com.ecommerce.repository;

import com.ecommerce.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProductInventory entity
 */
@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, UUID> {
    
    /**
     * Find inventory by product ID
     * @param productId product UUID
     * @return Optional containing inventory if exists
     */
    Optional<ProductInventory> findByProductId(UUID productId);
}