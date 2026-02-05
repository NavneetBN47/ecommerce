package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private Long id;

    private String orderNumber;

    private Long userId;

    private List<OrderItemDTO> items;

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private AddressDTO shippingAddress;

    private Order.OrderStatus status;

    private BigDecimal totalAmount;

    private BigDecimal shippingCost;

    private BigDecimal taxAmount;

    private LocalDateTime orderDate;

    private String paymentMethod;

    private String paymentStatus;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}