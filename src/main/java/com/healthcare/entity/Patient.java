package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Patient entity representing a healthcare patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the patient", example = "1")
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Schema(description = "Patient's first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Schema(description = "Patient's last name", example = "Doe", required = true)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    @Schema(description = "Patient's date of birth", example = "1990-01-15", required = true)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Column(nullable = false, length = 10)
    @Schema(description = "Patient's gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"}, required = true)
    private String gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 255)
    @Schema(description = "Patient's email address", example = "john.doe@email.com", required = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(nullable = false, length = 20)
    @Schema(description = "Patient's phone number", example = "+1234567890", required = true)
    private String phoneNumber;

    @Size(max = 20)
    @Column(length = 20)
    @Schema(description = "Patient's social security number (encrypted)", example = "***-**-1234")
    private String ssn;

    @Column(length = 50)
    @Schema(description = "Patient's blood type", example = "A+")
    private String bloodType;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Patient's known allergies", example = "Penicillin, Peanuts")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Patient's medical history summary")
    private String medicalHistory;

    @Column(nullable = false)
    @Schema(description = "Patient account active status", example = "true")
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when patient record was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Timestamp when patient record was last updated", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of patient addresses")
    private List<PatientAddress> addresses;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of emergency contacts")
    private List<EmergencyContact> emergencyContacts;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of insurance information")
    private List<InsuranceInfo> insuranceInfo;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "List of patient consents")
    private List<PatientConsent> consents;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @Schema(description = "List of appointments")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @Schema(description = "List of medical records")
    private List<MedicalRecord> medicalRecords;
}