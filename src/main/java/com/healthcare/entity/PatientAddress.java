package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patient_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Patient address information")
public class PatientAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the address", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @Schema(description = "Associated patient")
    private Patient patient;

    @NotBlank(message = "Address type is required")
    @Pattern(regexp = "^(HOME|WORK|BILLING|SHIPPING)$")
    @Column(nullable = false, length = 20)
    @Schema(description = "Type of address", example = "HOME", allowableValues = {"HOME", "WORK", "BILLING", "SHIPPING"}, required = true)
    private String addressType;

    @NotBlank(message = "Street address is required")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    @Schema(description = "Street address", example = "123 Main Street", required = true)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Schema(description = "City", example = "New York", required = true)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    @Schema(description = "State or province", example = "NY", required = true)
    private String state;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    @Schema(description = "Postal/ZIP code", example = "10001", required = true)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Schema(description = "Country", example = "USA", required = true)
    private String country;

    @Column(nullable = false)
    @Schema(description = "Primary address flag", example = "true")
    private Boolean isPrimary = false;
}