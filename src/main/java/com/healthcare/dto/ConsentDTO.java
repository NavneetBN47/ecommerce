package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for Patient Consent")
public class ConsentDTO {

    @Schema(description = "Consent ID", example = "1")
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Schema(description = "Patient ID", example = "123", required = true)
    private Long patientId;

    @NotBlank(message = "Consent type is required")
    @Pattern(regexp = "^(TREATMENT|DISCLOSURE|RESEARCH|MARKETING)$")
    @Schema(description = "Type of consent", example = "TREATMENT", allowableValues = {"TREATMENT", "DISCLOSURE", "RESEARCH", "MARKETING"}, required = true)
    private String consentType;

    @NotNull(message = "Consent status is required")
    @Schema(description = "Consent granted status", example = "true", required = true)
    private Boolean granted;

    @Schema(description = "Consent description or terms")
    private String description;

    @Schema(description = "Timestamp when consent expires", example = "2025-01-15T10:30:00")
    private LocalDateTime expirationDate;

    @Schema(description = "Person who obtained the consent", example = "Dr. Smith")
    private String obtainedBy;
}