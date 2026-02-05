package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * TokenBlacklist Entity for invalidated JWT tokens
 * 
 * Business Rules:
 * - Stores invalidated tokens (logout, password change)
 * - Tokens expire after configured time
 * - Periodic cleanup of expired entries
 */
@Entity
@Table(name = "token_blacklist", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_expires", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Check if token is expired and can be removed
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}