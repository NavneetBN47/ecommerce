package com.ecommerce.usermanagement.application.service;

import com.ecommerce.usermanagement.application.dto.*;
import com.ecommerce.usermanagement.application.exception.*;
import com.ecommerce.usermanagement.application.mapper.UserMapper;
import com.ecommerce.usermanagement.domain.entity.User;
import com.ecommerce.usermanagement.domain.repository.UserRepository;
import com.ecommerce.usermanagement.infrastructure.security.JwtTokenService;
import com.ecommerce.usermanagement.infrastructure.security.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + request.getUsername() + " already exists");
        }
        
        String passwordHash = passwordService.hashPassword(request.getPassword());
        
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordHash)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .address(request.getAddress())
            .role(User.UserRole.CUSTOMER)
            .status(User.UserStatus.ACTIVE)
            .build();
        
        user = userRepository.save(user);
        auditLogService.logUserRegistration(user.getId(), user.getEmail());
        
        log.info("User registered successfully with ID: {}", user.getId());
        return userMapper.toRegistrationResponse(user);
    }

    @Transactional
    public UserLoginResponse loginUser(UserLoginRequest request) {
        log.info("User login attempt for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        
        if (!passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            auditLogService.logFailedLogin(user.getId(), user.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User account is not active");
        }
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        auditLogService.logSuccessfulLogin(user.getId(), user.getEmail());
        
        log.info("User logged in successfully: {}", user.getEmail());
        return UserLoginResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        
        user = userRepository.save(user);
        auditLogService.logProfileUpdate(user.getId(), user.getEmail());
        
        log.info("Profile updated successfully for user ID: {}", userId);
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public MessageResponse changePassword(Long userId, PasswordChangeRequest request) {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        if (!passwordService.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        String newPasswordHash = passwordService.hashPassword(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
        
        auditLogService.logPasswordChange(user.getId(), user.getEmail());
        
        log.info("Password changed successfully for user ID: {}", userId);
        return new MessageResponse("Password changed successfully");
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));
        
        // In production, this would send an email with reset token
        // For now, we'll just log it
        auditLogService.logPasswordResetRequest(user.getId(), user.getEmail());
        
        log.info("Password reset email sent to: {}", request.getEmail());
        return new MessageResponse("Password reset instructions sent to your email");
    }
}