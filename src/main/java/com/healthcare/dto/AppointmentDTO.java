package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
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
@Schema(description = "Data transfer object for Appointment")
public class AppointmentDTO {

    @Schema(description = "Appointment ID", example = "1")
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Schema(description = "Patient ID", example = "123", required = true)
    private Long patientId;

    @NotBlank(message = "Provider ID is required")
    @Schema(description = "Healthcare provider identifier", example = "PROV123", required = true)
    private String providerId;

    @NotBlank(message = "Provider name is required")
    @Schema(description = "Healthcare provider name", example = "Dr. Jane Smith", required = true)
    private String providerName;

    @NotNull(message = "Appointment date/time is required")
    @Future(message = "Appointment must be in the future")
    @Schema(description = "Appointment date and time", example = "2024-02-15T14:30:00", required = true)
    private LocalDateTime appointmentDateTime;

    @NotNull(message = "Duration is required")
    @Schema(description = "Appointment duration in minutes", example = "30", required = true)
    private Integer durationMinutes;

    @NotBlank(message = "Appointment type is required")
    @Pattern(regexp = "^(CONSULTATION|FOLLOW_UP|EMERGENCY|ROUTINE|SPECIALIST)$")
    @Schema(description = "Type of appointment", example = "CONSULTATION", allowableValues = {"CONSULTATION", "FOLLOW_UP", "EMERGENCY", "ROUTINE", "SPECIALIST"}, required = true)
    private String appointmentType;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(SCHEDULED|CONFIRMED|CANCELLED|COMPLETED|NO_SHOW)$")
    @Schema(description = "Appointment status", example = "SCHEDULED", allowableValues = {"SCHEDULED", "CONFIRMED", "CANCELLED", "COMPLETED", "NO_SHOW"}, required = true)
    private String status;

    @Schema(description = "Reason for appointment")
    private String reason;

    @Schema(description = "Additional notes")
    private String notes;
}