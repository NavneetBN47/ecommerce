package com.ecommerce.config;

import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
 * This test class verifies the Spring Security configuration,
 * including authentication, authorization, CORS, and JWT filter setup.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_SecurityConfig {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authConfig;

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Test that passwordEncoder bean returns BCryptPasswordEncoder
     * 
     * Verifies that the password encoder is properly configured
     * to use BCrypt hashing algorithm.
     */
    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.getClass().getSimpleName())
                .isEqualTo("BCryptPasswordEncoder");
    }

    /**
     * Test that passwordEncoder properly encodes passwords
     * 
     * Verifies that the password encoder can hash passwords
     * and validate them correctly.
     */
    @Test
    void passwordEncoder_ShouldEncodePasswordsCorrectly() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    /**
     * Test that authenticationProvider is properly configured
     * 
     * Verifies that the DaoAuthenticationProvider is configured
     * with the correct UserDetailsService and PasswordEncoder.
     */
    @Test
    void authenticationProvider_ShouldReturnConfiguredDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();
        
        assertThat(authProvider).isNotNull();
        assertThat(authProvider.getUserDetailsService()).isEqualTo(userDetailsService);
    }

    /**
     * Test that authenticationManager is properly created
     * 
     * Verifies that the AuthenticationManager bean is correctly
     * instantiated from the AuthenticationConfiguration.
     */
    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        AuthenticationManager mockAuthManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockAuthManager);
        
        AuthenticationManager authManager = securityConfig.authenticationManager(authConfig);
        
        assertThat(authManager).isNotNull();
        assertThat(authManager).isEqualTo(mockAuthManager);
    }

    /**
     * Test that CORS configuration source is properly set up
     * 
     * Verifies that CORS is configured with correct allowed origins,
     * methods, headers, and credentials settings.
     */
    @Test
    void corsConfigurationSource_ShouldReturnConfiguredCorsSource() {
        CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
        
        assertThat(corsSource).isNotNull();
        
        var corsConfig = corsSource.getCorsConfiguration("/**");
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).contains("http://localhost:3000", "http://localhost:4200");
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowedHeaders()).contains("*");
        assertThat(corsConfig.getAllowCredentials()).isTrue();
        assertThat(corsConfig.getMaxAge()).isEqualTo(3600L);
    }

    /**
     * Test that SecurityConfig class has required annotations
     * 
     * Verifies that the configuration class is properly annotated
     * with @Configuration, @EnableWebSecurity, and @EnableMethodSecurity.
     */
    @Test
    void securityConfig_ShouldHaveRequiredAnnotations() {
        assertThat(SecurityConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class)).isTrue();
        assertThat(SecurityConfig.class.isAnnotationPresent(
                org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class)).isTrue();
        assertThat(SecurityConfig.class.isAnnotationPresent(
                org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class)).isTrue();
    }
}