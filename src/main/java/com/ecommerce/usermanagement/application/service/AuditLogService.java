package com.ecommerce.usermanagement.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditLogService {
    
    public void logUserRegistration(Long userId, String email) {
        log.info("AUDIT: User registered - UserID: {}, Email: {}", userId, email);
    }
    
    public void logSuccessfulLogin(Long userId, String email) {
        log.info("AUDIT: Successful login - UserID: {}, Email: {}", userId, email);
    }
    
    public void logFailedLogin(Long userId, String email) {
        log.warn("AUDIT: Failed login attempt - UserID: {}, Email: {}", userId, email);
    }
    
    public void logProfileUpdate(Long userId, String email) {
        log.info("AUDIT: Profile updated - UserID: {}, Email: {}", userId, email);
    }
    
    public void logPasswordChange(Long userId, String email) {
        log.info("AUDIT: Password changed - UserID: {}, Email: {}", userId, email);
    }
    
    public void logPasswordResetRequest(Long userId, String email) {
        log.info("AUDIT: Password reset requested - UserID: {}, Email: {}", userId, email);
    }
}