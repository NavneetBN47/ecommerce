package com.ecommerce.dto;

import com.ecommerce.entity.Address;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Address Data Transfer Object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDTO {

    private Long id;
    private Long userId;

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private Address.AddressType addressType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}