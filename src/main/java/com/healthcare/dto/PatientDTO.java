package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for Patient")
public class PatientDTO {

    @Schema(description = "Patient ID", example = "1")
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Schema(description = "Patient's first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Schema(description = "Patient's last name", example = "Doe", required = true)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Patient's date of birth", example = "1990-01-15", required = true)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
    @Schema(description = "Patient's gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"}, required = true)
    private String gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Patient's email address", example = "john.doe@email.com", required = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @Schema(description = "Patient's phone number", example = "+1234567890", required = true)
    private String phoneNumber;

    @Size(max = 50)
    @Schema(description = "Patient's blood type", example = "A+")
    private String bloodType;

    @Schema(description = "Patient's known allergies", example = "Penicillin, Peanuts")
    private String allergies;

    @Schema(description = "Patient's medical history summary")
    private String medicalHistory;

    @Schema(description = "Patient account active status", example = "true")
    private Boolean active;
}