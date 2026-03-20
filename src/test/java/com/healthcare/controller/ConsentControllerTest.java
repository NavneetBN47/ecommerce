package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.ConsentDTO;
import com.healthcare.entity.PatientConsent;
import com.healthcare.service.ConsentService;
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

@WebMvcTest(ConsentController.class)
@DisplayName("Consent Controller Tests")
public class ConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsentService consentService;

    private PatientConsent testConsent;
    private ConsentDTO testConsentDTO;

    @BeforeEach
    void setUp() {
        testConsent = new PatientConsent();
        testConsent.setId(1L);
        testConsent.setPatientId(1L);
        testConsent.setConsentType("DATA_SHARING");
        testConsent.setConsentGiven(true);
        testConsent.setConsentDate(LocalDateTime.now());
        testConsent.setPurpose("Medical research");
        testConsent.setExpiryDate(LocalDateTime.now().plusYears(1));

        testConsentDTO = new ConsentDTO();
        testConsentDTO.setPatientId(1L);
        testConsentDTO.setConsentType("DATA_SHARING");
        testConsentDTO.setConsentGiven(true);
        testConsentDTO.setPurpose("Medical research");
        testConsentDTO.setExpiryDate(LocalDateTime.now().plusYears(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Consent - Valid Request")
    void testCreateConsent_ValidRequest() throws Exception {
        when(consentService.createConsent(any(ConsentDTO.class))).thenReturn(testConsent);

        mockMvc.perform(post("/api/consents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testConsentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.consentGiven").value(true));

        verify(consentService, times(1)).createConsent(any(ConsentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Consent - Missing Required Fields")
    void testCreateConsent_MissingRequiredFields() throws Exception {
        testConsentDTO.setPatientId(null);
        testConsentDTO.setConsentType(null);

        mockMvc.perform(post("/api/consents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testConsentDTO)))
                .andExpect(status().isBadRequest());

        verify(consentService, never()).createConsent(any(ConsentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Create Consent - Invalid Expiry Date")
    void testCreateConsent_InvalidExpiryDate() throws Exception {
        testConsentDTO.setExpiryDate(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/consents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testConsentDTO)))
                .andExpect(status().isBadRequest());

        verify(consentService, never()).createConsent(any(ConsentDTO.class));
    }

    @Test
    @DisplayName("Create Consent - Unauthorized")
    void testCreateConsent_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/consents")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testConsentDTO)))
                .andExpect(status().isUnauthorized());

        verify(consentService, never()).createConsent(any(ConsentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Consent By ID - Valid Request")
    void testGetConsentById_ValidRequest() throws Exception {
        when(consentService.getConsentById(1L)).thenReturn(Optional.of(testConsent));

        mockMvc.perform(get("/api/consents/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.consentType").value("DATA_SHARING"));

        verify(consentService, times(1)).getConsentById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Consent By ID - Not Found")
    void testGetConsentById_NotFound() throws Exception {
        when(consentService.getConsentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/consents/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(consentService, times(1)).getConsentById(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Consents By Patient ID - Valid Request")
    void testGetConsentsByPatientId_ValidRequest() throws Exception {
        List<PatientConsent> consents = Arrays.asList(testConsent);
        when(consentService.getConsentsByPatientId(1L)).thenReturn(consents);

        mockMvc.perform(get("/api/consents/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));

        verify(consentService, times(1)).getConsentsByPatientId(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Consents By Patient ID - Empty Result")
    void testGetConsentsByPatientId_EmptyResult() throws Exception {
        when(consentService.getConsentsByPatientId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/consents/patient/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(consentService, times(1)).getConsentsByPatientId(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Update Consent - Valid Request")
    void testUpdateConsent_ValidRequest() throws Exception {
        testConsent.setConsentGiven(false);
        when(consentService.updateConsent(eq(1L), any(ConsentDTO.class))).thenReturn(testConsent);

        testConsentDTO.setConsentGiven(false);

        mockMvc.perform(put("/api/consents/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testConsentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consentGiven").value(false));

        verify(consentService, times(1)).updateConsent(eq(1L), any(ConsentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Update Consent - Not Found")
    void testUpdateConsent_NotFound() throws Exception {
        when(consentService.updateConsent(eq(999L), any(ConsentDTO.class)))
                .thenThrow(new RuntimeException("Consent not found"));

        mockMvc.perform(put("/api/consents/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testConsentDTO)))
                .andExpect(status().isNotFound());

        verify(consentService, times(1)).updateConsent(eq(999L), any(ConsentDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Revoke Consent - Valid Request")
    void testRevokeConsent_ValidRequest() throws Exception {
        testConsent.setConsentGiven(false);
        when(consentService.revokeConsent(1L)).thenReturn(testConsent);

        mockMvc.perform(patch("/api/consents/1/revoke")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consentGiven").value(false));

        verify(consentService, times(1)).revokeConsent(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Revoke Consent - Not Found")
    void testRevokeConsent_NotFound() throws Exception {
        when(consentService.revokeConsent(999L))
                .thenThrow(new RuntimeException("Consent not found"));

        mockMvc.perform(patch("/api/consents/999/revoke")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(consentService, times(1)).revokeConsent(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete Consent - Valid Request")
    void testDeleteConsent_ValidRequest() throws Exception {
        doNothing().when(consentService).deleteConsent(1L);

        mockMvc.perform(delete("/api/consents/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(consentService, times(1)).deleteConsent(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Active Consents By Patient ID - Valid Request")
    void testGetActiveConsentsByPatientId_ValidRequest() throws Exception {
        List<PatientConsent> consents = Arrays.asList(testConsent);
        when(consentService.getActiveConsentsByPatientId(1L)).thenReturn(consents);

        mockMvc.perform(get("/api/consents/patient/1/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].consentGiven").value(true));

        verify(consentService, times(1)).getActiveConsentsByPatientId(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Get Consents By Type - Valid Request")
    void testGetConsentsByType_ValidRequest() throws Exception {
        List<PatientConsent> consents = Arrays.asList(testConsent);
        when(consentService.getConsentsByType("DATA_SHARING")).thenReturn(consents);

        mockMvc.perform(get("/api/consents/type/DATA_SHARING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].consentType").value("DATA_SHARING"));

        verify(consentService, times(1)).getConsentsByType("DATA_SHARING");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get Expiring Consents - Valid Request")
    void testGetExpiringConsents_ValidRequest() throws Exception {
        List<PatientConsent> consents = Arrays.asList(testConsent);
        when(consentService.getExpiringConsents(30)).thenReturn(consents);

        mockMvc.perform(get("/api/consents/expiring")
                .param("days", "30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(consentService, times(1)).getExpiringConsents(30);
    }
}