package com.ecommerce.service;

import com.ecommerce.dto.LoginRequestDTO;
import com.ecommerce.dto.LoginResponseDTO;
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
 * Service class for authentication operations
 * Implements stateless login with JWT tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CartService cartService;

    /**
     * Authenticate user and generate JWT token (stateless)
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Attempting login for user: {}", request.getIdentifier());

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getIdentifier())
            .orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));

        // Check if user is active
        if (!user.getActive()) {
            throw new AuthenticationException("User account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username/email or password");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        Long expiresIn = jwtTokenProvider.getExpirationTime();

        log.info("User logged in successfully: {}", user.getUsername());

        return LoginResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .user(mapUserToDTO(user))
            .build();
    }

    /**
     * Logout user and cleanup cart
     */
    public void logout(Long userId) {
        log.info("Logging out user: {}", userId);
        
        // Clear user's cart on logout
        cartService.clearCart(userId);
        
        log.info("User logged out successfully: {}", userId);
    }

    /**
     * Map User entity to UserDTO
     */
    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .active(user.getActive())
            .build();
    }
}