package com.example.ecommerce.config;

import com.example.ecommerce.service.JwtService;
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

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JwtAuthenticationFilter.
 * Tests JWT token validation and authentication filtering functionality.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws IOException {
        when(response.getWriter()).thenReturn(writer);
    }

    /**
     * Test that filter skips authentication for signup endpoint.
     */
    @Test
    void doFilterInternal_SignupEndpoint_ShouldSkipAuthentication() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/signup");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    /**
     * Test that filter skips authentication for login endpoint.
     */
    @Test
    void doFilterInternal_LoginEndpoint_ShouldSkipAuthentication() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/login");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    /**
     * Test that filter skips authentication for product search endpoint.
     */
    @Test
    void doFilterInternal_ProductSearchEndpoint_ShouldSkipAuthentication() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/products/search");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    /**
     * Test that filter returns 401 when Authorization header is missing.
     */
    @Test
    void doFilterInternal_MissingAuthHeader_ShouldReturn401() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write(contains("Missing or invalid token"));
        verify(filterChain, never()).doFilter(request, response);
    }

    /**
     * Test that filter returns 401 when Authorization header doesn't start with Bearer.
     */
    @Test
    void doFilterInternal_InvalidAuthHeader_ShouldReturn401() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write(contains("Missing or invalid token"));
        verify(filterChain, never()).doFilter(request, response);
    }

    /**
     * Test successful JWT authentication.
     */
    @Test
    void doFilterInternal_ValidJwtToken_ShouldSetUserIdAndContinue() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).validateToken(token);
        verify(jwtService).extractUserId(token);
        verify(request).setAttribute("userId", userId);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test that filter returns 401 when JWT token is invalid.
     */
    @Test
    void doFilterInternal_InvalidJwtToken_ShouldReturn401() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write(contains("Invalid or expired token"));
        verify(filterChain, never()).doFilter(request, response);
    }

    /**
     * Test that filter handles JWT service exceptions gracefully.
     */
    @Test
    void doFilterInternal_JwtServiceException_ShouldReturn401() throws ServletException, IOException {
        // Given
        String token = "malformed.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenThrow(new RuntimeException("Invalid JWT"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write(contains("Token validation failed"));
        verify(filterChain, never()).doFilter(request, response);
    }
}