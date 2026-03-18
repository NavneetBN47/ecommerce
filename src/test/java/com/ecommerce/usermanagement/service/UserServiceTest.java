package com.ecommerce.usermanagement.service;

import com.ecommerce.usermanagement.dto.AuthResponse;
import com.ecommerce.usermanagement.dto.LoginRequest;
import com.ecommerce.usermanagement.dto.RegisterRequest;
import com.ecommerce.usermanagement.dto.UserResponse;
import com.ecommerce.usermanagement.entity.User;
import com.ecommerce.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("John");
        user.setLastName("Doe");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");
    }

    @Test
    @DisplayName("Register User - Success")
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenService.generateToken(anyString())).thenReturn("jwt-token-123");

        AuthResponse response = userService.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(1L, response.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Email Already Exists")
    void testRegisterUser_EmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login User - Success")
    void testLoginUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenService.generateToken(anyString())).thenReturn("jwt-token-123");

        AuthResponse response = userService.loginUser(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Login User - Invalid Email")
    void testLoginUser_InvalidEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.loginUser(loginRequest));
    }

    @Test
    @DisplayName("Login User - Invalid Password")
    void testLoginUser_InvalidPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.loginUser(loginRequest));
    }

    @Test
    @DisplayName("Get User By ID - Success")
    void testGetUserById_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
    }

    @Test
    @DisplayName("Get User By ID - Not Found")
    void testGetUserById_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("Update User - Success")
    void testUpdateUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }
}
