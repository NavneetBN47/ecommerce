package com.ecommerce.config;

import com.ecommerce.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip authentication for signup and login endpoints
        if (path.equals("/api/users/signup") || path.equals("/api/users/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Missing or invalid token\"}");
            return;
        }

        String token = authHeader.substring(7);
        
        if (!jwtService.validateToken(token)) {
            log.error("Invalid JWT token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid token\"}");
            return;
        }

        try {
            UUID userId = jwtService.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid token\"}");
        }
    }
}