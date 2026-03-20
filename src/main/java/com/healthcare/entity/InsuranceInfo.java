package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "insurance_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Patient insurance information")
public class InsuranceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for insurance info", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @Schema(description = "Associated patient")
    private Patient patient;

    @NotBlank(message = "Provider name is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    @Schema(description = "Insurance provider name", example = "Blue Cross Blue Shield", required = true)
    private String providerName;

    @NotBlank(message = "Policy number is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Schema(description = "Insurance policy number", example = "POL123456789", required = true)
    private String policyNumber;

    @NotBlank(message = "Group number is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Schema(description = "Insurance group number", example = "GRP987654321", required = true)
    private String groupNumber;

    @NotNull(message = "Effective date is required")
    @Column(nullable = false)
    @Schema(description = "Policy effective date", example = "2024-01-01", required = true)
    private LocalDate effectiveDate;

    @Column
    @Schema(description = "Policy expiration date", example = "2024-12-31")
    private LocalDate expirationDate;

    @Column(nullable = false)
    @Schema(description = "Primary insurance flag", example = "true")
    private Boolean isPrimary = false;

    @Column(nullable = false)
    @Schema(description = "Insurance active status", example = "true")
    private Boolean active = true;
}