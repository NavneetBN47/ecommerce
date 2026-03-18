package com.ecommerce.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Cart item information")
public class CartItemResponse {
    
    @Schema(description = "Cart item ID", example = "1")
    private Long id;
    
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @Schema(description = "Product name", example = "Dell XPS 15 Laptop")
    private String productName;
    
    @Schema(description = "Product price", example = "1299.99")
    private BigDecimal productPrice;
    
    @Schema(description = "Quantity", example = "2")
    private Integer quantity;
    
    @Schema(description = "Subtotal (price × quantity)", example = "2599.98")
    private BigDecimal subtotal;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}