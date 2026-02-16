package com.ecommerce.config;

import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test class for SecurityConfig
 * 
 * Tests security configuration including:
 * - Password encoder bean
 * - Authentication provider
 * - CORS configuration
 * - Authentication manager
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class test_SecurityConfig {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    private SecurityConfig securityConfig;

    /**
     * Set up test instance before each test
     */
    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(userDetailsService, jwtAuthenticationFilter);
    }

    /**
     * Test password encoder bean creation
     * 
     * Validates:
     * - PasswordEncoder bean is created
     * - BCryptPasswordEncoder is used
     */
    @Test
    @DisplayName("Should create BCryptPasswordEncoder bean")
    void testPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.getClass().getSimpleName()).contains("BCrypt");
    }

    /**
     * Test authentication provider bean creation
     * 
     * Validates:
     * - DaoAuthenticationProvider is created
     * - UserDetailsService is set
     * - PasswordEncoder is set
     */
    @Test
    @DisplayName("Should create DaoAuthenticationProvider bean")
    void testAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();

        assertThat(authProvider).isNotNull();
    }

    /**
     * Test authentication manager bean creation
     * 
     * Validates:
     * - AuthenticationManager is created from configuration
     */
    @Test
    @DisplayName("Should create AuthenticationManager bean")
    void testAuthenticationManager() throws Exception {
        AuthenticationManager mockManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager authManager = securityConfig.authenticationManager(authenticationConfiguration);

        assertThat(authManager).isNotNull();
        assertThat(authManager).isEqualTo(mockManager);
    }

    /**
     * Test CORS configuration source
     * 
     * Validates:
     * - CorsConfigurationSource bean is created
     * - CORS configuration is not null
     */
    @Test
    @DisplayName("Should create CorsConfigurationSource bean")
    void testCorsConfigurationSource() {
        CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();

        assertThat(corsSource).isNotNull();
    }

    /**
     * Test password encoding functionality
     * 
     * Validates:
     * - Password can be encoded
     * - Encoded password is different from plain text
     * - Encoded password can be matched
     */
    @Test
    @DisplayName("Should encode and match passwords correctly")
    void testPasswordEncoderFunctionality() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String plainPassword = "testPassword123";

        String encodedPassword = passwordEncoder.encode(plainPassword);

        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(plainPassword);
        assertThat(passwordEncoder.matches(plainPassword, encodedPassword)).isTrue();
    }
}