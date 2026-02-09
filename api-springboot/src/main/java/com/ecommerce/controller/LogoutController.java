package com.ecommerce.controller;

import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Logout Controller - REST API endpoint for logout
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class LogoutController {

    private final CartService cartService;

    /**
     * POST /api/logout - Logout user and cleanup cart
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(@CurrentUser UserPrincipal currentUser) {
        log.info("Logout request for user: {}", currentUser.getId());
        
        // Cleanup cart on logout
        cartService.cleanupCartOnLogout(currentUser.getId());
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}