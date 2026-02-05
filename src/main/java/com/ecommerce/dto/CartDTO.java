package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Cart entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDTO {

    private Long id;

    private Long userId;

    private List<CartItemDTO> items;

    private Integer totalItems;

    private BigDecimal totalPrice;

    private Boolean isEmpty;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}