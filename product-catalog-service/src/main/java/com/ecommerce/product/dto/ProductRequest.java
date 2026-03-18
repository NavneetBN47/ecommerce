package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Product creation/update request")
public class ProductRequest {
    
    @Schema(description = "Product SKU (Stock Keeping Unit)", example = "LAPTOP-001", required = true)
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @Schema(description = "Product name", example = "Dell XPS 15 Laptop", required = true)
    @NotBlank(message = "Product name is required")
    private String name;
    
    @Schema(description = "Product description", example = "High-performance laptop with 16GB RAM and 512GB SSD")
    private String description;
    
    @Schema(description = "Product price", example = "1299.99", required = true)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Schema(description = "Stock quantity", example = "50", required = true)
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    @Schema(description = "Product category", example = "Electronics")
    private String category;
    
    @Schema(description = "Product brand", example = "Dell")
    private String brand;
    
    @Schema(description = "Product image URL", example = "https://example.com/images/laptop.jpg")
    private String imageUrl;
    
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
}