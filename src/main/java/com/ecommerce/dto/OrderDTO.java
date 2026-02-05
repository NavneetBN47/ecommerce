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
 * DTO for Order entity
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

    private String username;

    private List<OrderItemDTO> items;

    private Order.OrderStatus status;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private BigDecimal shippingAmount;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal grandTotal;

    @NotNull(message = "Shipping address is required")
    private Long shippingAddressId;

    private Long billingAddressId;

    private Order.PaymentMethod paymentMethod;

    private Order.PaymentStatus paymentStatus;

    private LocalDateTime orderDate;

    private LocalDateTime shippedDate;

    private LocalDateTime deliveredDate;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}