package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private Long id;

    private String orderNumber;

    private Long userId;

    private LocalDateTime orderDate;

    private String status;

    private BigDecimal totalAmount;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String billingAddress;

    private String paymentMethod;

    private String notes;

    private List<OrderItemDTO> items = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}