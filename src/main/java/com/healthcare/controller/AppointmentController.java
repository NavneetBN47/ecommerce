package com.healthcare.controller;

import com.healthcare.dto.AppointmentDTO;
import com.healthcare.service.AppointmentService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "APIs for managing patient appointments")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(
        summary = "Schedule a new appointment",
        description = "Creates a new appointment for a patient with a healthcare provider"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Appointment scheduled successfully",
            content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or time slot unavailable",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<AppointmentDTO> scheduleAppointment(
        @Parameter(description = "Appointment data to schedule", required = true)
        @Valid @RequestBody AppointmentDTO appointmentDTO
    ) {
        AppointmentDTO created = appointmentService.scheduleAppointment(appointmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Get appointment by ID",
        description = "Retrieves appointment details by appointment ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment found",
            content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
            content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(
        @Parameter(description = "Appointment ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    @Operation(
        summary = "Get appointments by patient ID",
        description = "Retrieves all appointments for a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of appointments retrieved successfully",
            content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content)
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(
        @Parameter(description = "Patient ID", required = true, example = "123")
        @PathVariable Long patientId
    ) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
        summary = "Get appointments by date range",
        description = "Retrieves appointments within a specified date range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of appointments retrieved successfully",
            content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content)
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDateRange(
        @Parameter(description = "Start date", required = true, example = "2024-01-01")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date", required = true, example = "2024-01-31")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
        summary = "Update appointment",
        description = "Updates an existing appointment's information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment updated successfully",
            content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
            content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(
        @Parameter(description = "Appointment ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated appointment data", required = true)
        @Valid @RequestBody AppointmentDTO appointmentDTO
    ) {
        AppointmentDTO updated = appointmentService.updateAppointment(id, appointmentDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Cancel appointment",
        description = "Cancels an existing appointment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully",
            content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
            content = @Content)
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
        @Parameter(description = "Appointment ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        AppointmentDTO cancelled = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(cancelled);
    }

    @Operation(
        summary = "Delete appointment",
        description = "Permanently deletes an appointment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Appointment deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
            content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(
        @Parameter(description = "Appointment ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}