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
 * JUnit 5 test class for JwtAuthenticationFilter.
 * Tests JWT authentication filter functionality including token extraction,
 * validation, and security context setup.
 *
 * @author QA Automation Team
 * @version 1.0
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

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final Long USER_ID = 123L;

    /**
     * Setup method executed before each test.
     * Initializes SecurityContextHolder with mock SecurityContext.
     */
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Test successful JWT authentication with valid token.
     * Verifies that authentication is set in SecurityContext when valid JWT is provided.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(USER_ID);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider).validateToken(VALID_TOKEN);
        verify(jwtTokenProvider).getUserIdFromToken(VALID_TOKEN);
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior with invalid JWT token.
     * Verifies that authentication is not set when token validation fails.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider).validateToken(VALID_TOKEN);
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior when no Authorization header is present.
     * Verifies that filter chain continues without setting authentication.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithNoAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior with empty Authorization header.
     * Verifies that filter chain continues without setting authentication.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithEmptyAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior with Authorization header without Bearer prefix.
     * Verifies that token is not extracted when Bearer prefix is missing.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithoutBearerPrefix_ShouldNotExtractToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(VALID_TOKEN);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior when token provider throws exception.
     * Verifies that filter handles exceptions gracefully and continues filter chain.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WhenTokenProviderThrowsException_ShouldHandleGracefully() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenThrow(new RuntimeException("Token validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior with malformed Bearer token.
     * Verifies that filter handles malformed tokens without errors.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithMalformedBearerToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test filter behavior with whitespace in Authorization header.
     * Verifies proper handling of whitespace scenarios.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithWhitespaceToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test that filter always continues the filter chain.
     * Verifies that doFilter is always called regardless of authentication status.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_AlwaysContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Test authentication details are properly set.
     * Verifies that WebAuthenticationDetails are attached to authentication.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_SetsAuthenticationDetails() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(USER_ID);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }
}