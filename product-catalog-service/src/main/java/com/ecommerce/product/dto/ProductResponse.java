package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Product information response")
public class ProductResponse {
    
    @Schema(description = "Product ID", example = "1")
    private Long id;
    
    @Schema(description = "Product SKU", example = "LAPTOP-001")
    private String sku;
    
    @Schema(description = "Product name", example = "Dell XPS 15 Laptop")
    private String name;
    
    @Schema(description = "Product description", example = "High-performance laptop with 16GB RAM and 512GB SSD")
    private String description;
    
    @Schema(description = "Product price", example = "1299.99")
    private BigDecimal price;
    
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stockQuantity;
    
    @Schema(description = "Product category", example = "Electronics")
    private String category;
    
    @Schema(description = "Product brand", example = "Dell")
    private String brand;
    
    @Schema(description = "Product image URL", example = "https://example.com/images/laptop.jpg")
    private String imageUrl;
    
    @Schema(description = "Product active status", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Product creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Product last update timestamp")
    private LocalDateTime updatedAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}