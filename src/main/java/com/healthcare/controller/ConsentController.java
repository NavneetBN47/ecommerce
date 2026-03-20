package com.healthcare.controller;

import com.healthcare.dto.ConsentDTO;
import com.healthcare.service.ConsentService;
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
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
@Tag(name = "Consent Management", description = "APIs for managing patient consents for HIPAA compliance")
@SecurityRequirement(name = "bearerAuth")
public class ConsentController {

    private final ConsentService consentService;

    @Operation(
        summary = "Record a new patient consent",
        description = "Records a new consent from a patient for treatment, disclosure, research, or marketing purposes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Consent recorded successfully",
            content = @Content(schema = @Schema(implementation = ConsentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<ConsentDTO> recordConsent(
        @Parameter(description = "Consent data to record", required = true)
        @Valid @RequestBody ConsentDTO consentDTO
    ) {
        ConsentDTO created = consentService.recordConsent(consentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Get consent by ID",
        description = "Retrieves a specific consent record by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consent found",
            content = @Content(schema = @Schema(implementation = ConsentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Consent not found",
            content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsentDTO> getConsentById(
        @Parameter(description = "Consent ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        ConsentDTO consent = consentService.getConsentById(id);
        return ResponseEntity.ok(consent);
    }

    @Operation(
        summary = "Get consents by patient ID",
        description = "Retrieves all consent records for a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of consents retrieved successfully",
            content = @Content(schema = @Schema(implementation = ConsentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ConsentDTO>> getConsentsByPatient(
        @Parameter(description = "Patient ID", required = true, example = "123")
        @PathVariable Long patientId
    ) {
        List<ConsentDTO> consents = consentService.getConsentsByPatient(patientId);
        return ResponseEntity.ok(consents);
    }

    @Operation(
        summary = "Update consent",
        description = "Updates an existing consent record"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consent updated successfully",
            content = @Content(schema = @Schema(implementation = ConsentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Consent not found",
            content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsentDTO> updateConsent(
        @Parameter(description = "Consent ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated consent data", required = true)
        @Valid @RequestBody ConsentDTO consentDTO
    ) {
        ConsentDTO updated = consentService.updateConsent(id, consentDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Revoke consent",
        description = "Revokes a patient's consent by setting granted status to false"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consent revoked successfully",
            content = @Content(schema = @Schema(implementation = ConsentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Consent not found",
            content = @Content)
    })
    @PatchMapping("/{id}/revoke")
    public ResponseEntity<ConsentDTO> revokeConsent(
        @Parameter(description = "Consent ID", required = "true", example = "1")
        @PathVariable Long id
    ) {
        ConsentDTO revoked = consentService.revokeConsent(id);
        return ResponseEntity.ok(revoked);
    }

    @Operation(
        summary = "Delete consent",
        description = "Permanently deletes a consent record"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Consent deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Consent not found",
            content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsent(
        @Parameter(description = "Consent ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        consentService.deleteConsent(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Check consent validity",
        description = "Checks if a patient has valid consent for a specific consent type"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consent validity checked",
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkConsentValidity(
        @Parameter(description = "Patient ID", required = true, example = "123")
        @RequestParam Long patientId,
        @Parameter(description = "Consent type", required = true, example = "TREATMENT")
        @RequestParam String consentType
    ) {
        Boolean isValid = consentService.checkConsentValidity(patientId, consentType);
        return ResponseEntity.ok(isValid);
    }
}