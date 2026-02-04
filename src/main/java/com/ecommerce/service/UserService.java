package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.InvalidCredentialsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // In production, use BCrypt
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUserId());
        
        return mapToUserResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public LoginResponse login(UserLoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        
        User user = userRepository.findByUsernameAndPassword(request.getUsername(), request.getPassword())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        
        String token = jwtService.generateToken(user.getUserId(), user.getUsername());
        
        log.info("User logged in successfully: {}", user.getUserId());
        
        return LoginResponse.builder()
            .token(token)
            .userId(user.getUserId())
            .username(user.getUsername())
            .build();
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        log.info("Fetching user profile: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        return mapToUserResponse(user);
    }
    
    @Transactional
    public UserResponse updateUserProfile(Long userId, UserRegistrationRequest request) {
        log.info("Updating user profile: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully: {}", userId);
        
        return mapToUserResponse(updatedUser);
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
