package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.AppointmentDTO;
import com.healthcare.entity.Appointment;
import com.healthcare.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@DisplayName("Appointment Controller Tests")
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private AppointmentDTO testAppointmentDTO;

    @BeforeEach
    void setUp() {
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setPatientId(1L);
        testAppointment.setProviderId(1L);
        testAppointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        testAppointment.setDurationMinutes(30);
        testAppointment.setAppointmentType("Consultation");
        testAppointment.setStatus("SCHEDULED");
        testAppointment.setReasonForVisit("Regular checkup");

        testAppointmentDTO = new AppointmentDTO();
        testAppointmentDTO.setPatientId(1L);
        testAppointmentDTO.setProviderId(1L);
        testAppointmentDTO.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        testAppointmentDTO.setDurationMinutes(30);
        testAppointmentDTO.setAppointmentType("Consultation");
        testAppointmentDTO.setReasonForVisit("Regular checkup");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Appointment - Valid Request")
    void testCreateAppointment_ValidRequest() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentDTO.class))).thenReturn(testAppointment);

        mockMvc.perform(post("/api/appointments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService, times(1)).createAppointment(any(AppointmentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Appointment - Past DateTime")
    void testCreateAppointment_PastDateTime() throws Exception {
        testAppointmentDTO.setAppointmentDateTime(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/appointments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointmentDTO)))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any(AppointmentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Appointment - Missing Required Fields")
    void testCreateAppointment_MissingRequiredFields() throws Exception {
        testAppointmentDTO.setPatientId(null);
        testAppointmentDTO.setProviderId(null);

        mockMvc.perform(post("/api/appointments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointmentDTO)))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any(AppointmentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Appointment - Invalid Duration")
    void testCreateAppointment_InvalidDuration() throws Exception {
        testAppointmentDTO.setDurationMinutes(-10);

        mockMvc.perform(post("/api/appointments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointmentDTO)))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any(AppointmentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Appointment By ID - Valid Request")
    void testGetAppointmentById_ValidRequest() throws Exception {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));

        mockMvc.perform(get("/api/appointments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService, times(1)).getAppointmentById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Appointment By ID - Not Found")
    void testGetAppointmentById_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/appointments/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(appointmentService, times(1)).getAppointmentById(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Appointments By Patient ID - Valid Request")
    void testGetAppointmentsByPatientId_ValidRequest() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentService.getAppointmentsByPatientId(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/appointments/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));

        verify(appointmentService, times(1)).getAppointmentsByPatientId(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Appointments By Provider ID - Valid Request")
    void testGetAppointmentsByProviderId_ValidRequest() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentService.getAppointmentsByProviderId(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/appointments/provider/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].providerId").value(1));

        verify(appointmentService, times(1)).getAppointmentsByProviderId(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Update Appointment - Valid Request")
    void testUpdateAppointment_ValidRequest() throws Exception {
        testAppointment.setStatus("CONFIRMED");
        when(appointmentService.updateAppointment(eq(1L), any(AppointmentDTO.class))).thenReturn(testAppointment);

        testAppointmentDTO.setStatus("CONFIRMED");

        mockMvc.perform(put("/api/appointments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(appointmentService, times(1)).updateAppointment(eq(1L), any(AppointmentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Cancel Appointment - Valid Request")
    void testCancelAppointment_ValidRequest() throws Exception {
        testAppointment.setStatus("CANCELLED");
        when(appointmentService.cancelAppointment(1L)).thenReturn(testAppointment);

        mockMvc.perform(patch("/api/appointments/1/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(appointmentService, times(1)).cancelAppointment(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Cancel Appointment - Not Found")
    void testCancelAppointment_NotFound() throws Exception {
        when(appointmentService.cancelAppointment(999L))
                .thenThrow(new RuntimeException("Appointment not found"));

        mockMvc.perform(patch("/api/appointments/999/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(appointmentService, times(1)).cancelAppointment(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete Appointment - Valid Request")
    void testDeleteAppointment_ValidRequest() throws Exception {
        doNothing().when(appointmentService).deleteAppointment(1L);

        mockMvc.perform(delete("/api/appointments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(appointmentService, times(1)).deleteAppointment(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Appointments By Status - Valid Request")
    void testGetAppointmentsByStatus_ValidRequest() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentService.getAppointmentsByStatus("SCHEDULED")).thenReturn(appointments);

        mockMvc.perform(get("/api/appointments/status/SCHEDULED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));

        verify(appointmentService, times(1)).getAppointmentsByStatus("SCHEDULED");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Appointments By Date Range - Valid Request")
    void testGetAppointmentsByDateRange_ValidRequest() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        
        when(appointmentService.getAppointmentsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/appointments/date-range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(appointmentService, times(1)).getAppointmentsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}