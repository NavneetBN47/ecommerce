package com.ecommerce.controller;

import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Authentication Controller
 * Handles logout and session cleanup
 * API Contracts as per LLD Section 4.3
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final CartService cartService;

    /**
     * POST /api/logout
     * Logout user and cleanup cart
     * Business Rule: Cart and all items deleted on logout
     * @param authentication current authenticated user
     * @return 200 OK with message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Logout request for user: {}", userId);
        
        cartService.logoutAndCleanupCart(userId);
        
        return ResponseEntity.ok(Map.of("message", "Logged out. Cart deleted."));
    }
}