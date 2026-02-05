package com.example.dto;

import com.example.entity.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private List<OrderItemDTO> items;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private String shippingAddress;
    private String billingAddress;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}