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
import java.util.ArrayList;
import java.util.List;

/**
 * Order Data Transfer Object
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
    private List<OrderItemDTO> items = new ArrayList<>();
    private Long shippingAddressId;
    private AddressDTO shippingAddress;
    private Order.OrderStatus status;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private Integer totalItems;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}