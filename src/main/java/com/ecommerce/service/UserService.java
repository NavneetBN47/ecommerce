package com.ecommerce.service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for User operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CartService cartService;
    
    /**
     * Register a new user
     */
    @Transactional
    public UserDTO registerUser(UserDTO userDTO) {
        log.info("Registering new user: {}", userDTO.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists: " + userDTO.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists: " + userDTO.getEmail());
        }
        
        User user = modelMapper.map(userDTO, User.class);
        user.setActive(true);
        
        // In production, password should be hashed using BCrypt or similar
        // For now, storing as plain text (NOT RECOMMENDED FOR PRODUCTION)
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());
        
        return modelMapper.map(savedUser, UserDTO.class);
    }
    
    /**
     * Login user - stateless authentication
     */
    @Transactional(readOnly = true)
    public UserDTO login(LoginRequest loginRequest) {
        log.info("User login attempt: {}", loginRequest.getUsernameOrEmail());
        
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
        
        // In production, use BCrypt password encoder to verify password
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new UnauthorizedException("Invalid username/email or password");
        }
        
        if (!user.getActive()) {
            throw new UnauthorizedException("User account is inactive");
        }
        
        log.info("User logged in successfully: {}", user.getId());
        return modelMapper.map(user, UserDTO.class);
    }
    
    /**
     * Logout user - cleanup cart if empty
     */
    @Transactional
    public void logout(Long userId) {
        log.info("User logout: {}", userId);
        
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        // Cleanup empty cart on logout
        cartService.cleanupEmptyCart(userId);
        
        log.info("User logged out successfully: {}", userId);
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return modelMapper.map(user, UserDTO.class);
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        
        return userRepository.findAll().stream()
            .map(user -> modelMapper.map(user, UserDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Update user
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if new username already exists (if changed)
        if (!user.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists: " + userDTO.getUsername());
        }
        
        // Check if new email already exists (if changed)
        if (!user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists: " + userDTO.getEmail());
        }
        
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(userDTO.getPassword());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());
        
        return modelMapper.map(updatedUser, UserDTO.class);
    }
    
    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        userRepository.delete(user);
        log.info("User deleted successfully: {}", id);
    }
}