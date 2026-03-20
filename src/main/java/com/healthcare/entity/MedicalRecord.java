package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Patient medical record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the medical record", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @Schema(description = "Associated patient")
    private Patient patient;

    @NotBlank(message = "Record type is required")
    @Column(nullable = false, length = 50)
    @Schema(description = "Type of medical record", example = "DIAGNOSIS", required = true)
    private String recordType;

    @NotNull(message = "Record date is required")
    @Column(nullable = false)
    @Schema(description = "Date of the medical record", example = "2024-01-15T10:30:00", required = true)
    private LocalDateTime recordDate;

    @NotBlank(message = "Provider ID is required")
    @Column(nullable = false, length = 100)
    @Schema(description = "Healthcare provider identifier", example = "PROV123", required = true)
    private String providerId;

    @NotBlank(message = "Provider name is required")
    @Column(nullable = false, length = 200)
    @Schema(description = "Healthcare provider name", example = "Dr. Jane Smith", required = true)
    private String providerName;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Diagnosis information")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Treatment information")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Prescribed medications")
    private String medications;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Additional notes")
    private String notes;

    @Column(nullable = false)
    @Schema(description = "Encryption status of sensitive data", example = "true")
    private Boolean encrypted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when record was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Timestamp when record was last updated", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of attachments")
    private List<Attachment> attachments;
}