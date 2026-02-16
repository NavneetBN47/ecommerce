package com.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for JwtAuthenticationFilter
 * 
 * Tests JWT authentication filter functionality including:
 * - Extracting JWT from request
 * - Validating JWT token
 * - Setting authentication in security context
 * - Handling invalid tokens
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class test_JwtAuthenticationFilter {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID testUserId;
    private UserPrincipal testUserPrincipal;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserPrincipal = new UserPrincipal(
            testUserId,
            "testuser",
            "password",
            "test@example.com",
            new ArrayList<>()
        );
        SecurityContextHolder.clearContext();
    }

    /**
     * Test successful JWT authentication
     * 
     * Validates:
     * - JWT is extracted from header
     * - Token is validated
     * - User details are loaded
     * - Authentication is set in security context
     */
    @Test
    @DisplayName("Should authenticate successfully with valid JWT")
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(testUserId.toString());
        when(userDetailsService.loadUserById(testUserId)).thenReturn(testUserPrincipal);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .isEqualTo(testUserPrincipal);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter with no authorization header
     * 
     * Validates:
     * - Filter continues without setting authentication
     * - No exceptions are thrown
     */
    @Test
    @DisplayName("Should continue filter chain when no authorization header")
    void testDoFilterInternal_NoAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test filter with invalid token format
     * 
     * Validates:
     * - Filter continues without authentication
     * - Invalid format is handled gracefully
     */
    @Test
    @DisplayName("Should continue filter chain when token format is invalid")
    void testDoFilterInternal_InvalidTokenFormat() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test filter with invalid JWT token
     * 
     * Validates:
     * - Invalid token is not authenticated
     * - Filter chain continues
     */
    @Test
    @DisplayName("Should continue filter chain when JWT validation fails")
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserById(any(UUID.class));
    }

    /**
     * Test filter with exception during authentication
     * 
     * Validates:
     * - Exception is caught and logged
     * - Filter chain continues
     * - No authentication is set
     */
    @Test
    @DisplayName("Should handle exception gracefully during authentication")
    void testDoFilterInternal_ExceptionHandling() throws ServletException, IOException {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(token)).thenThrow(new RuntimeException("Test exception"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter with empty bearer token
     * 
     * Validates:
     * - Empty token is handled
     * - No authentication is set
     */
    @Test
    @DisplayName("Should handle empty bearer token")
    void testDoFilterInternal_EmptyBearerToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}