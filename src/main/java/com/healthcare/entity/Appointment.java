package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Patient appointment information")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the appointment", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @Schema(description = "Associated patient")
    private Patient patient;

    @NotBlank(message = "Provider ID is required")
    @Column(nullable = false, length = 100)
    @Schema(description = "Healthcare provider identifier", example = "PROV123", required = true)
    private String providerId;

    @NotBlank(message = "Provider name is required")
    @Column(nullable = false, length = 200)
    @Schema(description = "Healthcare provider name", example = "Dr. Jane Smith", required = true)
    private String providerName;

    @NotNull(message = "Appointment date/time is required")
    @Future(message = "Appointment must be in the future")
    @Column(nullable = false)
    @Schema(description = "Appointment date and time", example = "2024-02-15T14:30:00", required = true)
    private LocalDateTime appointmentDateTime;

    @NotNull(message = "Duration is required")
    @Column(nullable = false)
    @Schema(description = "Appointment duration in minutes", example = "30", required = true)
    private Integer durationMinutes;

    @NotBlank(message = "Appointment type is required")
    @Pattern(regexp = "^(CONSULTATION|FOLLOW_UP|EMERGENCY|ROUTINE|SPECIALIST)$")
    @Column(nullable = false, length = 50)
    @Schema(description = "Type of appointment", example = "CONSULTATION", allowableValues = {"CONSULTATION", "FOLLOW_UP", "EMERGENCY", "ROUTINE", "SPECIALIST"}, required = true)
    private String appointmentType;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(SCHEDULED|CONFIRMED|CANCELLED|COMPLETED|NO_SHOW)$")
    @Column(nullable = false, length = 20)
    @Schema(description = "Appointment status", example = "SCHEDULED", allowableValues = {"SCHEDULED", "CONFIRMED", "CANCELLED", "COMPLETED", "NO_SHOW"}, required = true)
    private String status;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Reason for appointment")
    private String reason;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Additional notes")
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when appointment was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Timestamp when appointment was last updated", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;
}