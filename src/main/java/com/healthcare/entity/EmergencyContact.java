package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Emergency contact information for a patient")
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the emergency contact", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @Schema(description = "Associated patient")
    private Patient patient;

    @NotBlank(message = "Contact name is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    @Schema(description = "Full name of emergency contact", example = "Jane Doe", required = true)
    private String name;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    @Schema(description = "Relationship to patient", example = "Spouse", required = true)
    private String relationship;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @Column(nullable = false, length = 20)
    @Schema(description = "Contact phone number", example = "+1234567890", required = true)
    private String phoneNumber;

    @Email
    @Size(max = 255)
    @Column(length = 255)
    @Schema(description = "Contact email address", example = "jane.doe@email.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Primary contact flag", example = "true")
    private Boolean isPrimary = false;
}