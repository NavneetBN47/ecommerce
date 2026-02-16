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
 * JUnit test class for JwtAuthenticationFilter.
 * Tests JWT authentication filter functionality including token validation,
 * authentication setting, and error handling.
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

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Setup method to clear SecurityContext before each test.
     */
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Test doFilterInternal with valid JWT token.
     * Verifies that authentication is set in SecurityContext when a valid JWT is provided.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithValidJwt_ShouldSetAuthentication() throws ServletException, IOException {
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
     * Test doFilterInternal with invalid JWT token.
     * Verifies that authentication is not set when JWT validation fails.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithInvalidJwt_ShouldNotSetAuthentication() throws ServletException, IOException {
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
     * Test doFilterInternal without JWT token.
     * Verifies that filter continues without setting authentication when no token is provided.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithoutJwt_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set without token");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
    }

    /**
     * Test doFilterInternal with malformed Authorization header.
     * Verifies that filter handles malformed headers gracefully.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithMalformedAuthHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for malformed header");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    /**
     * Test doFilterInternal with empty Bearer token.
     * Verifies that filter handles empty tokens correctly.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithEmptyBearerToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for empty token");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Test doFilterInternal when exception occurs during token processing.
     * Verifies that exceptions are handled gracefully and filter chain continues.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WhenExceptionOccurs_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenThrow(new RuntimeException("Token processing error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set when exception occurs");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Test doFilterInternal with valid JWT but null userId.
     * Verifies handling of edge case where token is valid but userId extraction fails.
     *
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Test
    void testDoFilterInternal_WithValidJwtButNullUserId_ShouldHandleGracefully() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        verify(filterChain, times(1)).doFilter(request, response);
    }
}