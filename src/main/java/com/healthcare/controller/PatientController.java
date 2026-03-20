package com.healthcare.controller;

import com.healthcare.dto.PatientDTO;
import com.healthcare.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "APIs for managing patient information")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    @Operation(
        summary = "Create a new patient",
        description = "Creates a new patient record in the system with all required information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Patient created successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Patient with email already exists",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(
        @Parameter(description = "Patient data to create", required = true)
        @Valid @RequestBody PatientDTO patientDTO
    ) {
        PatientDTO created = patientService.createPatient(patientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Get patient by ID",
        description = "Retrieves a patient's complete information by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient found",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(
        @Parameter(description = "Patient ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @Operation(
        summary = "Get all patients",
        description = "Retrieves a list of all patients in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of patients retrieved successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @Operation(
        summary = "Update patient information",
        description = "Updates an existing patient's information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient updated successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
        @Parameter(description = "Patient ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated patient data", required = true)
        @Valid @RequestBody PatientDTO patientDTO
    ) {
        PatientDTO updated = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Delete patient",
        description = "Soft deletes a patient by marking them as inactive"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Patient deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(
        @Parameter(description = "Patient ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Search patients by email",
        description = "Finds a patient by their email address"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient found",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<PatientDTO> searchPatientByEmail(
        @Parameter(description = "Patient email address", required = true, example = "john.doe@email.com")
        @RequestParam String email
    ) {
        PatientDTO patient = patientService.findByEmail(email);
        return ResponseEntity.ok(patient);
    }
}