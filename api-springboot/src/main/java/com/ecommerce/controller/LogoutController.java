package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for logout operations
 * Implements LLD logout with cart cleanup
 */
@RestController
@RequestMapping("/logout")
@Tag(name = "Authentication", description = "APIs for authentication operations")
public class LogoutController {

    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    @Autowired
    private CartService cartService;

    /**
     * Logout endpoint with cart cleanup
     * Implements LLD POST /api/logout
     */
    @PostMapping
    @Operation(summary = "User logout", description = "Logout user and clear shopping cart")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Logout request received for user ID: {}", userId);

        // Clear cart on logout per LLD requirement
        cartService.clearCartOnLogout(userId);

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}