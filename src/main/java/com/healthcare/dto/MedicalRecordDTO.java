package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for Medical Record")
public class MedicalRecordDTO {

    @Schema(description = "Medical record ID", example = "1")
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Schema(description = "Patient ID", example = "123", required = true)
    private Long patientId;

    @NotBlank(message = "Record type is required")
    @Schema(description = "Type of medical record", example = "DIAGNOSIS", required = true)
    private String recordType;

    @NotNull(message = "Record date is required")
    @Schema(description = "Date of the medical record", example = "2024-01-15T10:30:00", required = true)
    private LocalDateTime recordDate;

    @NotBlank(message = "Provider ID is required")
    @Schema(description = "Healthcare provider identifier", example = "PROV123", required = true)
    private String providerId;

    @NotBlank(message = "Provider name is required")
    @Schema(description = "Healthcare provider name", example = "Dr. Jane Smith", required = true)
    private String providerName;

    @Schema(description = "Diagnosis information")
    private String diagnosis;

    @Schema(description = "Treatment information")
    private String treatment;

    @Schema(description = "Prescribed medications")
    private String medications;

    @Schema(description = "Additional notes")
    private String notes;
}