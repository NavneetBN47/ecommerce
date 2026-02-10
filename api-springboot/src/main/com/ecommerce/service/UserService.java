package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserResponse signup(SignupRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return mapToUserResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUsername());
                    return new ResourceNotFoundException("User not found");
                });
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        log.info("User logged in successfully: {}", user.getUsername());
        return mapToUserResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });
        
        return mapToUserResponse(user);
    }
    
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", updatedUser.getUsername());
        
        return mapToUserResponse(updatedUser);
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}