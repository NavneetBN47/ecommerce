package com.ecommerce.usermanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password Service Tests")
class PasswordServiceTest {

    private PasswordService passwordService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
        passwordService = new PasswordService(passwordEncoder);
    }

    @Test
    @DisplayName("Should hash password successfully")
    void testHashPassword_Success() {
        String plainPassword = "SecurePass123!";

        String hashedPassword = passwordService.hashPassword(plainPassword);

        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(hashedPassword).startsWith("$2a$12$");
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void testHashPassword_DifferentSalts() {
        String plainPassword = "SecurePass123!";

        String hash1 = passwordService.hashPassword(plainPassword);
        String hash2 = passwordService.hashPassword(plainPassword);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordService.matches(plainPassword, hash1)).isTrue();
        assertThat(passwordService.matches(plainPassword, hash2)).isTrue();
    }

    @Test
    @DisplayName("Should match password with correct hash")
    void testMatches_CorrectPassword() {
        String plainPassword = "SecurePass123!";
        String hashedPassword = passwordService.hashPassword(plainPassword);

        boolean result = passwordService.matches(plainPassword, hashedPassword);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not match password with incorrect hash")
    void testMatches_IncorrectPassword() {
        String plainPassword = "SecurePass123!";
        String wrongPassword = "WrongPass456!";
        String hashedPassword = passwordService.hashPassword(plainPassword);

        boolean result = passwordService.matches(wrongPassword, hashedPassword);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should validate strong password")
    void testValidatePassword_Strong() {
        String strongPassword = "SecurePass123!@#";

        boolean result = passwordService.validatePasswordStrength(strongPassword);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should reject weak password - too short")
    void testValidatePassword_TooShort() {
        String shortPassword = "Pass1!";

        boolean result = passwordService.validatePasswordStrength(shortPassword);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject weak password - no uppercase")
    void testValidatePassword_NoUppercase() {
        String noUppercasePassword = "securepass123!";

        boolean result = passwordService.validatePasswordStrength(noUppercasePassword);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject weak password - no lowercase")
    void testValidatePassword_NoLowercase() {
        String noLowercasePassword = "SECUREPASS123!";

        boolean result = passwordService.validatePasswordStrength(noLowercasePassword);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject weak password - no digit")
    void testValidatePassword_NoDigit() {
        String noDigitPassword = "SecurePass!@#";

        boolean result = passwordService.validatePasswordStrength(noDigitPassword);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject weak password - no special character")
    void testValidatePassword_NoSpecialChar() {
        String noSpecialCharPassword = "SecurePass123";

        boolean result = passwordService.validatePasswordStrength(noSpecialCharPassword);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject common passwords")
    void testValidatePassword_CommonPassword() {
        String[] commonPasswords = {"Password123!", "Admin123!", "Welcome123!"};

        for (String password : commonPasswords) {
            boolean result = passwordService.isCommonPassword(password);
            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("Should generate random password with correct length")
    void testGenerateRandomPassword() {
        int length = 16;

        String randomPassword = passwordService.generateRandomPassword(length);

        assertThat(randomPassword).hasSize(length);
        assertThat(passwordService.validatePasswordStrength(randomPassword)).isTrue();
    }
}