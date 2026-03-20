package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.MedicalRecordDTO;
import com.healthcare.entity.MedicalRecord;
import com.healthcare.service.MedicalRecordService;
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

@WebMvcTest(MedicalRecordController.class)
@DisplayName("Medical Record Controller Tests")
public class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalRecordService medicalRecordService;

    private MedicalRecord testMedicalRecord;
    private MedicalRecordDTO testMedicalRecordDTO;

    @BeforeEach
    void setUp() {
        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setId(1L);
        testMedicalRecord.setPatientId(1L);
        testMedicalRecord.setProviderId(1L);
        testMedicalRecord.setAppointmentId(1L);
        testMedicalRecord.setRecordType("CONSULTATION");
        testMedicalRecord.setDiagnosis("Common cold");
        testMedicalRecord.setTreatment("Rest and fluids");
        testMedicalRecord.setPrescription("Paracetamol 500mg");
        testMedicalRecord.setNotes("Patient recovering well");
        testMedicalRecord.setRecordDate(LocalDateTime.now());

        testMedicalRecordDTO = new MedicalRecordDTO();
        testMedicalRecordDTO.setPatientId(1L);
        testMedicalRecordDTO.setProviderId(1L);
        testMedicalRecordDTO.setAppointmentId(1L);
        testMedicalRecordDTO.setRecordType("CONSULTATION");
        testMedicalRecordDTO.setDiagnosis("Common cold");
        testMedicalRecordDTO.setTreatment("Rest and fluids");
        testMedicalRecordDTO.setPrescription("Paracetamol 500mg");
        testMedicalRecordDTO.setNotes("Patient recovering well");
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Create Medical Record - Valid Request")
    void testCreateMedicalRecord_ValidRequest() throws Exception {
        when(medicalRecordService.createMedicalRecord(any(MedicalRecordDTO.class))).thenReturn(testMedicalRecord);

        mockMvc.perform(post("/api/medical-records")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMedicalRecordDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.diagnosis").value("Common cold"));

        verify(medicalRecordService, times(1)).createMedicalRecord(any(MedicalRecordDTO.class));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Create Medical Record - Missing Required Fields")
    void testCreateMedicalRecord_MissingRequiredFields() throws Exception {
        testMedicalRecordDTO.setPatientId(null);
        testMedicalRecordDTO.setProviderId(null);

        mockMvc.perform(post("/api/medical-records")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMedicalRecordDTO)))
                .andExpect(status().isBadRequest());

        verify(medicalRecordService, never()).createMedicalRecord(any(MedicalRecordDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Medical Record - Insufficient Permissions")
    void testCreateMedicalRecord_InsufficientPermissions() throws Exception {
        mockMvc.perform(post("/api/medical-records")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMedicalRecordDTO)))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never()).createMedicalRecord(any(MedicalRecordDTO.class));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Get Medical Record By ID - Valid Request")
    void testGetMedicalRecordById_ValidRequest() throws Exception {
        when(medicalRecordService.getMedicalRecordById(1L)).thenReturn(Optional.of(testMedicalRecord));

        mockMvc.perform(get("/api/medical-records/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.diagnosis").value("Common cold"));

        verify(medicalRecordService, times(1)).getMedicalRecordById(1L);
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Get Medical Record By ID - Not Found")
    void testGetMedicalRecordById_NotFound() throws Exception {
        when(medicalRecordService.getMedicalRecordById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medical-records/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(medicalRecordService, times(1)).getMedicalRecordById(999L);
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Get Medical Records By Patient ID - Valid Request")
    void testGetMedicalRecordsByPatientId_ValidRequest() throws Exception {
        List<MedicalRecord> records = Arrays.asList(testMedicalRecord);
        when(medicalRecordService.getMedicalRecordsByPatientId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/medical-records/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));

        verify(medicalRecordService, times(1)).getMedicalRecordsByPatientId(1L);
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Get Medical Records By Patient ID - Empty Result")
    void testGetMedicalRecordsByPatientId_EmptyResult() throws Exception {
        when(medicalRecordService.getMedicalRecordsByPatientId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/medical-records/patient/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(medicalRecordService, times(1)).getMedicalRecordsByPatientId(999L);
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Update Medical Record - Valid Request")
    void testUpdateMedicalRecord_ValidRequest() throws Exception {
        testMedicalRecord.setDiagnosis("Severe cold");
        when(medicalRecordService.updateMedicalRecord(eq(1L), any(MedicalRecordDTO.class))).thenReturn(testMedicalRecord);

        testMedicalRecordDTO.setDiagnosis("Severe cold");

        mockMvc.perform(put("/api/medical-records/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMedicalRecordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Severe cold"));

        verify(medicalRecordService, times(1)).updateMedicalRecord(eq(1L), any(MedicalRecordDTO.class));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Update Medical Record - Not Found")
    void testUpdateMedicalRecord_NotFound() throws Exception {
        when(medicalRecordService.updateMedicalRecord(eq(999L), any(MedicalRecordDTO.class)))
                .thenThrow(new RuntimeException("Medical record not found"));

        mockMvc.perform(put("/api/medical-records/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMedicalRecordDTO)))
                .andExpect(status().isNotFound());

        verify(medicalRecordService, times(1)).updateMedicalRecord(eq(999L), any(MedicalRecordDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete Medical Record - Valid Request")
    void testDeleteMedicalRecord_ValidRequest() throws Exception {
        doNothing().when(medicalRecordService).deleteMedicalRecord(1L);

        mockMvc.perform(delete("/api/medical-records/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(medicalRecordService, times(1)).deleteMedicalRecord(1L);
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Get Medical Records By Record Type - Valid Request")
    void testGetMedicalRecordsByRecordType_ValidRequest() throws Exception {
        List<MedicalRecord> records = Arrays.asList(testMedicalRecord);
        when(medicalRecordService.getMedicalRecordsByRecordType("CONSULTATION")).thenReturn(records);

        mockMvc.perform(get("/api/medical-records/type/CONSULTATION")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordType").value("CONSULTATION"));

        verify(medicalRecordService, times(1)).getMedicalRecordsByRecordType("CONSULTATION");
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Get Medical Records By Date Range - Valid Request")
    void testGetMedicalRecordsByDateRange_ValidRequest() throws Exception {
        List<MedicalRecord> records = Arrays.asList(testMedicalRecord);
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        when(medicalRecordService.getMedicalRecordsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(records);

        mockMvc.perform(get("/api/medical-records/date-range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(medicalRecordService, times(1)).getMedicalRecordsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    @DisplayName("Search Medical Records - Valid Request")
    void testSearchMedicalRecords_ValidRequest() throws Exception {
        List<MedicalRecord> records = Arrays.asList(testMedicalRecord);
        when(medicalRecordService.searchMedicalRecords("cold")).thenReturn(records);

        mockMvc.perform(get("/api/medical-records/search")
                .param("keyword", "cold")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diagnosis").value("Common cold"));

        verify(medicalRecordService, times(1)).searchMedicalRecords("cold");
    }
}