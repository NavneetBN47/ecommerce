package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.PatientDTO;
import com.healthcare.entity.Patient;
import com.healthcare.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@DisplayName("Patient Controller Tests")
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private Patient testPatient;
    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setGender("Male");
        testPatient.setEmail("john.doe@example.com");
        testPatient.setPhoneNumber("+1234567890");
        testPatient.setSsn("123-45-6789");

        testPatientDTO = new PatientDTO();
        testPatientDTO.setFirstName("John");
        testPatientDTO.setLastName("Doe");
        testPatientDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatientDTO.setGender("Male");
        testPatientDTO.setEmail("john.doe@example.com");
        testPatientDTO.setPhoneNumber("+1234567890");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create Patient - Valid Request")
    void testCreatePatient_ValidRequest() throws Exception {
        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(testPatient);

        mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(patientService, times(1)).createPatient(any(PatientDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create Patient - Invalid Email")
    void testCreatePatient_InvalidEmail() throws Exception {
        testPatientDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any(PatientDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create Patient - Missing Required Fields")
    void testCreatePatient_MissingRequiredFields() throws Exception {
        testPatientDTO.setFirstName(null);
        testPatientDTO.setLastName(null);

        mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any(PatientDTO.class));
    }

    @Test
    @DisplayName("Create Patient - Unauthorized")
    void testCreatePatient_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isUnauthorized());

        verify(patientService, never()).createPatient(any(PatientDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Patient By ID - Valid Request")
    void testGetPatientById_ValidRequest() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(Optional.of(testPatient));

        mockMvc.perform(get("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(patientService, times(1)).getPatientById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Patient By ID - Not Found")
    void testGetPatientById_NotFound() throws Exception {
        when(patientService.getPatientById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(patientService, times(1)).getPatientById(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get All Patients - Valid Request")
    void testGetAllPatients_ValidRequest() throws Exception {
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientService.getAllPatients()).thenReturn(patients);

        mockMvc.perform(get("/api/patients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update Patient - Valid Request")
    void testUpdatePatient_ValidRequest() throws Exception {
        testPatient.setFirstName("Jane");
        when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(testPatient);

        testPatientDTO.setFirstName("Jane");

        mockMvc.perform(put("/api/patients/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(patientService, times(1)).updatePatient(eq(1L), any(PatientDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update Patient - Not Found")
    void testUpdatePatient_NotFound() throws Exception {
        when(patientService.updatePatient(eq(999L), any(PatientDTO.class)))
                .thenThrow(new RuntimeException("Patient not found"));

        mockMvc.perform(put("/api/patients/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isNotFound());

        verify(patientService, times(1)).updatePatient(eq(999L), any(PatientDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete Patient - Valid Request")
    void testDeletePatient_ValidRequest() throws Exception {
        doNothing().when(patientService).deletePatient(1L);

        mockMvc.perform(delete("/api/patients/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(patientService, times(1)).deletePatient(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete Patient - Not Found")
    void testDeletePatient_NotFound() throws Exception {
        doThrow(new RuntimeException("Patient not found")).when(patientService).deletePatient(999L);

        mockMvc.perform(delete("/api/patients/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(patientService, times(1)).deletePatient(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Search Patients By Name - Valid Request")
    void testSearchPatientsByName_ValidRequest() throws Exception {
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientService.searchPatientsByName("John")).thenReturn(patients);

        mockMvc.perform(get("/api/patients/search")
                .param("name", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));

        verify(patientService, times(1)).searchPatientsByName("John");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Search Patients By Name - Empty Result")
    void testSearchPatientsByName_EmptyResult() throws Exception {
        when(patientService.searchPatientsByName("NonExistent")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/patients/search")
                .param("name", "NonExistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(patientService, times(1)).searchPatientsByName("NonExistent");
    }
}