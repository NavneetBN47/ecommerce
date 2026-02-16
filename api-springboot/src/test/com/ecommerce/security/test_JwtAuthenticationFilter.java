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
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for JwtAuthenticationFilter
 * Tests JWT authentication filter functionality including token extraction and validation
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

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Test successful JWT authentication with valid token
     * Verifies that authentication is set in SecurityContext when valid JWT is provided
     */
    @Test
    void testDoFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        Long userId = 123L;
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should be set in SecurityContext");
        assertEquals(userId.toString(), authentication.getPrincipal(), "User ID should match");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, times(1)).validateToken(validToken);
        verify(jwtTokenProvider, times(1)).getUserIdFromToken(validToken);
    }

    /**
     * Test JWT authentication with invalid token
     * Verifies that authentication is not set when token validation fails
     */
    @Test
    void testDoFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for invalid token");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, times(1)).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
    }

    /**
     * Test JWT authentication without Authorization header
     * Verifies that filter continues without setting authentication when no token is provided
     */
    @Test
    void testDoFilterInternal_WithoutAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set when no token provided");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
    }

    /**
     * Test JWT authentication with empty Authorization header
     * Verifies that filter handles empty authorization header gracefully
     */
    @Test
    void testDoFilterInternal_WithEmptyAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for empty header");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test JWT authentication with malformed Authorization header (missing Bearer prefix)
     * Verifies that filter handles malformed authorization header correctly
     */
    @Test
    void testDoFilterInternal_WithMalformedAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for malformed header");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test JWT authentication when token validation throws exception
     * Verifies that filter handles exceptions gracefully and continues filter chain
     */
    @Test
    void testDoFilterInternal_WhenValidationThrowsException_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set when exception occurs");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Test JWT authentication with Bearer token containing only whitespace
     * Verifies edge case handling for whitespace-only tokens
     */
    @Test
    void testDoFilterInternal_WithWhitespaceToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for whitespace token");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Test JWT authentication with valid token but getUserIdFromToken throws exception
     * Verifies that filter handles user ID extraction errors gracefully
     */
    @Test
    void testDoFilterInternal_WhenGetUserIdThrowsException_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenThrow(new RuntimeException("User ID extraction error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set when user ID extraction fails");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}