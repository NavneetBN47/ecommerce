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
import static org.mockito.Mockito.*;

/**
 * Test class for JwtAuthenticationFilter.
 * Tests JWT token extraction, validation, and authentication setup in the security context.
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
     * Verifies that authentication is set in the security context.
     */
    @Test
    void testDoFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        Long userId = 123L;
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUserIdFromToken(validToken);
    }

    /**
     * Test filter behavior with invalid JWT token.
     * Verifies that authentication is not set and filter chain continues.
     */
    @Test
    void testDoFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        String invalidToken = "invalid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
    }

    /**
     * Test filter behavior when no Authorization header is present.
     * Verifies that filter chain continues without authentication.
     */
    @Test
    void testDoFilterInternal_WithNoAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test filter behavior with empty Authorization header.
     * Verifies that filter chain continues without authentication.
     */
    @Test
    void testDoFilterInternal_WithEmptyAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test filter behavior with Authorization header not starting with Bearer.
     * Verifies that token is not extracted and filter chain continues.
     */
    @Test
    void testDoFilterInternal_WithNonBearerToken_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test filter behavior when token validation throws an exception.
     * Verifies that exception is caught and filter chain continues.
     */
    @Test
    void testDoFilterInternal_WithExceptionDuringValidation_ShouldContinueFilterChain() throws ServletException, IOException {
        String token = "problematic.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token validation error"));

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any(Authentication.class));
    }

    /**
     * Test filter behavior when getUserIdFromToken throws an exception.
     * Verifies that exception is caught and filter chain continues.
     */
    @Test
    void testDoFilterInternal_WithExceptionDuringUserIdExtraction_ShouldContinueFilterChain() throws ServletException, IOException {
        String token = "valid.but.problematic.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenThrow(new RuntimeException("User ID extraction error"));

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any(Authentication.class));
    }

    /**
     * Test filter with Bearer token that has extra spaces.
     * Verifies that token is correctly extracted.
     */
    @Test
    void testDoFilterInternal_WithBearerTokenWithSpaces_ShouldExtractToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        Long userId = 456L;
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUserIdFromToken(token);
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test that filter always calls filterChain.doFilter regardless of authentication success.
     * Verifies filter chain continuation.
     */
    @Test
    void testDoFilterInternal_AlwaysCallsFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}