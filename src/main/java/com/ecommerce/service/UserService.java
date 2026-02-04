package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    
    @Transactional
    public UserProfileResponse signup(UserSignupRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return mapToProfileResponse(savedUser);
    }
    
    public LoginResponse login(UserLoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.warn("Login failed - user not found: {}", request.getUsername());
                return new InvalidCredentialsException("Invalid username or password");
            });
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - invalid password for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername());
        log.info("User logged in successfully: {}", user.getUsername());
        
        return LoginResponse.builder()
            .token(token)
            .username(user.getUsername())
            .message("Login successful")
            .build();
    }
    
    public UserProfileResponse getProfile(UUID userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User not found: {}", userId);
                return new ResourceNotFoundException("User not found");
            });
        
        return mapToProfileResponse(user);
    }
    
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UserProfileUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User not found: {}", userId);
                return new ResourceNotFoundException("User not found");
            });
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", updatedUser.getUsername());
        
        return mapToProfileResponse(updatedUser);
    }
    
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .build();
    }
}