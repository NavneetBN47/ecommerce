package com.healthcare.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.PatientDTO;
import com.healthcare.entity.Patient;
import com.healthcare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Patient Integration Tests")
public class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

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
    @DisplayName("Integration Test - Create and Retrieve Patient")
    void testCreateAndRetrievePatient() throws Exception {
        // Create patient
        String createResponse = mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Patient createdPatient = objectMapper.readValue(createResponse, Patient.class);

        // Retrieve patient
        mockMvc.perform(get("/api/patients/" + createdPatient.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdPatient.getId()))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration Test - Create, Update and Retrieve Patient")
    void testCreateUpdateAndRetrievePatient() throws Exception {
        // Create patient
        String createResponse = mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Patient createdPatient = objectMapper.readValue(createResponse, Patient.class);

        // Update patient
        testPatientDTO.setFirstName("Jane");
        mockMvc.perform(put("/api/patients/" + createdPatient.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        // Retrieve updated patient
        mockMvc.perform(get("/api/patients/" + createdPatient.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration Test - Create and Delete Patient")
    void testCreateAndDeletePatient() throws Exception {
        // Create patient
        String createResponse = mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Patient createdPatient = objectMapper.readValue(createResponse, Patient.class);

        // Delete patient
        mockMvc.perform(delete("/api/patients/" + createdPatient.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/patients/" + createdPatient.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration Test - Search Patients")
    void testSearchPatients() throws Exception {
        // Create multiple patients
        mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated());

        testPatientDTO.setFirstName("Jane");
        testPatientDTO.setEmail("jane.doe@example.com");
        mockMvc.perform(post("/api/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated());

        // Search for patients
        mockMvc.perform(get("/api/patients/search")
                .param("name", "Doe")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}