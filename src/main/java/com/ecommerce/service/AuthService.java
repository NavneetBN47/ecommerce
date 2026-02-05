package com.ecommerce.service;

import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.AuthenticationException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service with stateless login
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CartService cartService;

    /**
     * Stateless login - returns JWT token
     */
    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        if (!user.getActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        String token = jwtTokenProvider.generateToken(user);
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(UserDTO userDTO) {
        log.info("Registering new user: {}", userDTO.getUsername());
        
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new AuthenticationException("Username already exists");
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        User user = User.builder()
            .username(userDTO.getUsername())
            .email(userDTO.getEmail())
            .password(passwordEncoder.encode(userDTO.getPassword()))
            .firstName(userDTO.getFirstName())
            .lastName(userDTO.getLastName())
            .phoneNumber(userDTO.getPhoneNumber())
            .active(true)
            .role(User.UserRole.CUSTOMER)
            .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser);
        
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .userId(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .role(savedUser.getRole())
            .build();
    }

    /**
     * Logout with cart cleanup
     */
    @Transactional
    public void logout(Long userId) {
        log.info("Logout for user: {}", userId);
        
        // Cleanup empty carts on logout
        cartService.cleanupEmptyCartsOnLogout(userId);
        
        log.info("User logged out successfully: {}", userId);
    }
}