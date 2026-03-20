package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_consents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Patient consent records for HIPAA compliance")
public class PatientConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the consent", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @Schema(description = "Associated patient")
    private Patient patient;

    @NotBlank(message = "Consent type is required")
    @Pattern(regexp = "^(TREATMENT|DISCLOSURE|RESEARCH|MARKETING)$")
    @Column(nullable = false, length = 50)
    @Schema(description = "Type of consent", example = "TREATMENT", allowableValues = {"TREATMENT", "DISCLOSURE", "RESEARCH", "MARKETING"}, required = true)
    private String consentType;

    @NotNull(message = "Consent status is required")
    @Column(nullable = false)
    @Schema(description = "Consent granted status", example = "true", required = true)
    private Boolean granted;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Consent description or terms")
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when consent was recorded", example = "2024-01-15T10:30:00")
    private LocalDateTime consentDate;

    @Column
    @Schema(description = "Timestamp when consent expires", example = "2025-01-15T10:30:00")
    private LocalDateTime expirationDate;

    @Column(length = 100)
    @Schema(description = "Person who obtained the consent", example = "Dr. Smith")
    private String obtainedBy;
}