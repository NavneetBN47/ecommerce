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
@Table(name = "access_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Access history for HIPAA audit trail")
public class AccessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Patient ID accessed", example = "123", required = true)
    private Long patientId;

    @Column(nullable = false, length = 100)
    @Schema(description = "User who accessed the data", example = "doctor@healthcare.com", required = true)
    private String accessedBy;

    @Column(nullable = false, length = 50)
    @Schema(description = "Access type", example = "READ", required = true)
    private String accessType;

    @Column(length = 100)
    @Schema(description = "Resource accessed", example = "MedicalRecord")
    private String resourceType;

    @Column
    @Schema(description = "Resource ID", example = "456")
    private Long resourceId;

    @Column(length = 45)
    @Schema(description = "IP address of the accessor", example = "192.168.1.1")
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Purpose of access")
    private String purpose;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp of access", example = "2024-01-15T10:30:00")
    private LocalDateTime accessedAt;
}