package com.ecommerce.service;

import com.ecommerce.dto.LoginRequestDTO;
import com.ecommerce.dto.LoginResponseDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service
 * Handles business logic for user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CartService cartService;

    /**
     * User sign-up (registration)
     * NO cart creation at signup
     */
    @Transactional
    public UserDTO signUp(UserDTO userDTO) {
        log.info("Attempting to register user: {}", userDTO.getUsername());
        
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            log.error("Username already exists: {}", userDTO.getUsername());
            throw new DuplicateResourceException("Username already exists: " + userDTO.getUsername());
        }
        
        User user = modelMapper.map(userDTO, User.class);
        User savedUser = userRepository.save(user);
        
        log.info("User registered successfully: {}", savedUser.getUsername());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    /**
     * User sign-in (login)
     * Stateless - no session management
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO signIn(LoginRequestDTO loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        User user = userRepository.findByUsernameAndPassword(
            loginRequest.getUsername(), 
            loginRequest.getPassword()
        ).orElseThrow(() -> {
            log.error("Invalid credentials for user: {}", loginRequest.getUsername());
            return new ResourceNotFoundException("Invalid username or password");
        });
        
        log.info("User logged in successfully: {}", user.getUsername());
        return new LoginResponseDTO(
            user.getUserId(),
            user.getUsername(),
            user.getFullName(),
            "Login successful"
        );
    }

    /**
     * User logout
     * Clears cart and cart items
     */
    @Transactional
    public void logout(Long userId) {
        log.info("Logout initiated for user ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            log.error("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        // Clear cart on logout
        cartService.clearCartOnLogout(userId);
        
        log.info("User logged out successfully: {}", userId);
    }

    /**
     * Get user profile
     */
    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found: {}", userId);
                return new ResourceNotFoundException("User not found with ID: " + userId);
            });
        
        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserDTO updateUserProfile(Long userId, UserDTO userDTO) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found: {}", userId);
                return new ResourceNotFoundException("User not found with ID: " + userId);
            });
        
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully: {}", userId);
        
        return modelMapper.map(updatedUser, UserDTO.class);
    }
}