package com.healthcare.controller;

import com.healthcare.dto.MedicalRecordDTO;
import com.healthcare.service.MedicalRecordService;
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
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Record Management", description = "APIs for managing patient medical records")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Operation(
        summary = "Create a new medical record",
        description = "Creates a new medical record for a patient with diagnosis, treatment, and medication information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Medical record created successfully",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(
        @Parameter(description = "Medical record data to create", required = true)
        @Valid @RequestBody MedicalRecordDTO medicalRecordDTO
    ) {
        MedicalRecordDTO created = medicalRecordService.createMedicalRecord(medicalRecordDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Get medical record by ID",
        description = "Retrieves a specific medical record by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Medical record found",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Medical record not found",
            content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecordById(
        @Parameter(description = "Medical record ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        MedicalRecordDTO record = medicalRecordService.getMedicalRecordById(id);
        return ResponseEntity.ok(record);
    }

    @Operation(
        summary = "Get medical records by patient ID",
        description = "Retrieves all medical records for a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of medical records retrieved successfully",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDTO>> getMedicalRecordsByPatient(
        @Parameter(description = "Patient ID", required = true, example = "123")
        @PathVariable Long patientId
    ) {
        List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByPatient(patientId);
        return ResponseEntity.ok(records);
    }

    @Operation(
        summary = "Update medical record",
        description = "Updates an existing medical record's information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Medical record updated successfully",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Medical record not found",
            content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
        @Parameter(description = "Medical record ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated medical record data", required = true)
        @Valid @RequestBody MedicalRecordDTO medicalRecordDTO
    ) {
        MedicalRecordDTO updated = medicalRecordService.updateMedicalRecord(id, medicalRecordDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Delete medical record",
        description = "Permanently deletes a medical record (use with caution - consider archiving instead)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Medical record deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Medical record not found",
            content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(
        @Parameter(description = "Medical record ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Search medical records by record type",
        description = "Retrieves medical records filtered by record type for a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of medical records retrieved successfully",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions",
            content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<List<MedicalRecordDTO>> searchMedicalRecordsByType(
        @Parameter(description = "Patient ID", required = true, example = "123")
        @RequestParam Long patientId,
        @Parameter(description = "Record type", required = true, example = "DIAGNOSIS")
        @RequestParam String recordType
    ) {
        List<MedicalRecordDTO> records = medicalRecordService.searchByType(patientId, recordType);
        return ResponseEntity.ok(records);
    }
}