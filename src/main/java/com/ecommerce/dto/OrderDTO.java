package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
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

    private String username;

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    private Order.OrderStatus status;

    private BigDecimal totalAmount;

    private String shippingAddress;

    private String billingAddress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}