package com.healthcare.service;

import com.healthcare.dto.PatientDTO;
import com.healthcare.entity.Patient;
import com.healthcare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Patient Service Tests")
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private AuditLoggerService auditLoggerService;

    @InjectMocks
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
    @DisplayName("Create Patient - Success")
    void testCreatePatient_Success() {
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        doNothing().when(auditLoggerService).logAction(anyString(), anyString(), anyString());

        Patient result = patientService.createPatient(testPatientDTO);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(auditLoggerService, times(1)).logAction(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Get Patient By ID - Found")
    void testGetPatientById_Found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        Optional<Patient> result = patientService.getPatientById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get Patient By ID - Not Found")
    void testGetPatientById_NotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Patient> result = patientService.getPatientById(999L);

        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Get All Patients - Success")
    void testGetAllPatients_Success() {
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> result = patientService.getAllPatients();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Update Patient - Success")
    void testUpdatePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        doNothing().when(auditLoggerService).logAction(anyString(), anyString(), anyString());

        Patient result = patientService.updatePatient(1L, testPatientDTO);

        assertNotNull(result);
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Update Patient - Not Found")
    void testUpdatePatient_NotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            patientService.updatePatient(999L, testPatientDTO);
        });

        verify(patientRepository, times(1)).findById(999L);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @DisplayName("Delete Patient - Success")
    void testDeletePatient_Success() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(1L);
        doNothing().when(auditLoggerService).logAction(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> patientService.deletePatient(1L));

        verify(patientRepository, times(1)).existsById(1L);
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete Patient - Not Found")
    void testDeletePatient_NotFound() {
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            patientService.deletePatient(999L);
        });

        verify(patientRepository, times(1)).existsById(999L);
        verify(patientRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Search Patients By Name - Found")
    void testSearchPatientsByName_Found() {
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByFirstNameContainingOrLastNameContaining("John", "John"))
                .thenReturn(patients);

        List<Patient> result = patientService.searchPatientsByName("John");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findByFirstNameContainingOrLastNameContaining("John", "John");
    }

    @Test
    @DisplayName("Search Patients By Name - Empty Result")
    void testSearchPatientsByName_EmptyResult() {
        when(patientRepository.findByFirstNameContainingOrLastNameContaining("NonExistent", "NonExistent"))
                .thenReturn(Arrays.asList());

        List<Patient> result = patientService.searchPatientsByName("NonExistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(patientRepository, times(1)).findByFirstNameContainingOrLastNameContaining("NonExistent", "NonExistent");
    }
}