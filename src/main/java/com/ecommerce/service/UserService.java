package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
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
    private final JwtService jwtService;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        log.info("Attempting to sign up user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);
        log.info("User signed up successfully: {}", savedUser.getUsername());

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting to login user: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("Invalid credentials for username: {}", request.getUsername());
                    return new UnauthorizedException("Invalid username or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.error("Invalid password for username: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getId().toString());
        log.info("User logged in successfully: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", updatedUser.getUsername());

        return mapToUserResponse(updatedUser);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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