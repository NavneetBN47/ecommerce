package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "System audit log for compliance")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Column(nullable = false, length = 50)
    @Schema(description = "Event type", example = "USER_LOGIN", required = true)
    private String eventType;

    @Column(nullable = false, length = 100)
    @Schema(description = "User who triggered the event", example = "admin@healthcare.com", required = true)
    private String userId;

    @Column(length = 100)
    @Schema(description = "Entity type affected", example = "Patient")
    private String entityType;

    @Column
    @Schema(description = "Entity ID affected", example = "123")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Event details (JSON)")
    private String details;

    @Column(length = 45)
    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Column(length = 20)
    @Schema(description = "Severity level", example = "INFO")
    private String severity;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp of the event", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
}