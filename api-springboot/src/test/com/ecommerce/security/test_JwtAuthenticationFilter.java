package com.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * Tests JWT token extraction, validation, and authentication context setup.
 */
@ExtendWith(MockitoExtension.class)
class test_JwtAuthenticationFilter {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Test successful JWT authentication with valid token.
     * Verifies that authentication is set in SecurityContext.
     */
    @Test
    void testDoFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        Long userId = 123L;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUserIdFromToken(validToken);
    }

    /**
     * Test JWT authentication with invalid token.
     * Verifies that authentication is not set when token is invalid.
     */
    @Test
    void testDoFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        String bearerToken = "Bearer " + invalidToken;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
    }

    /**
     * Test JWT authentication with missing Authorization header.
     * Verifies that filter continues without setting authentication.
     */
    @Test
    void testDoFilterInternal_WithNoAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test JWT authentication with empty Authorization header.
     * Verifies that filter handles empty header gracefully.
     */
    @Test
    void testDoFilterInternal_WithEmptyAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test JWT authentication with Authorization header without Bearer prefix.
     * Verifies that token without Bearer prefix is ignored.
     */
    @Test
    void testDoFilterInternal_WithoutBearerPrefix_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test JWT authentication when token validation throws exception.
     * Verifies that exceptions are handled gracefully and filter continues.
     */
    @Test
    void testDoFilterInternal_WhenTokenValidationThrowsException_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenThrow(new RuntimeException("Token validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test JWT authentication with Bearer token containing only whitespace.
     * Verifies that whitespace-only tokens are handled correctly.
     */
    @Test
    void testDoFilterInternal_WithBearerAndWhitespace_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test that filter chain is always called regardless of authentication success.
     * Verifies filter chain continuation in all scenarios.
     */
    @Test
    void testDoFilterInternal_AlwaysCallsFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }
}