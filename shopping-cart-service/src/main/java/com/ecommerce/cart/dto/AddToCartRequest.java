package com.ecommerce.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to add item to cart")
public class AddToCartRequest {
    
    @Schema(description = "Product ID", example = "1", required = true)
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Schema(description = "Quantity to add", example = "2", required = true)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}